/**
 * The (Su)bversion Re(po)sitory (S)earch (E)ngine (SupoSE for short).
 *
 * Copyright (c) 2007-2011 by SoftwareEntwicklung Beratung Schulung (SoEBeS)
 * Copyright (c) 2007-2011 by Karl Heinz Marbaise
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301 USA
 *
 * The License can viewed online under http://www.gnu.org/licenses/gpl.html
 * If you have any questions about the Software or about the license
 * just write an email to license@soebes.de
 */
package com.soebes.supose.core.scan.document;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.apache.log4j.Logger;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.sax.BodyContentHandler;
import org.tmatesoft.svn.core.SVNDirEntry;
import org.tmatesoft.svn.core.SVNException;
import org.xml.sax.helpers.DefaultHandler;

import com.soebes.supose.core.FieldNames;
import com.soebes.supose.core.repository.Repository;

/**
 * This class will scan an XML document.
 *
 * @author Karl Heinz Marbaise
 */
public class ScanXMLDocument extends AScanDocument {
    private static Logger LOGGER = Logger.getLogger(ScanXMLDocument.class);

    public ScanXMLDocument() {
    }

    @Override
    public void indexDocument(Repository repository, SVNDirEntry dirEntry,
            String path, long revision) {
        LOGGER.debug("Scanning document");
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            // This means we get the contents of the file only. No properties.
            repository.getRepository().getFile(path, revision, null, baos);
            ByteArrayInputStream str = new ByteArrayInputStream(
                    baos.toByteArray());
            scan(str, path, dirEntry);
        } catch (SVNException e) {
            LOGGER.error(
                    "Exception by SVN " + path + " (r" + dirEntry.getRevision()
                    + ")", e);
        } catch (Exception e) {
            LOGGER.error("Other Exception with the XML document " + path
                    + " (r" + dirEntry.getRevision() + ")", e);
        }
    }

    private void scan(ByteArrayInputStream in, String path, SVNDirEntry dirEntry) {
        try {
            Metadata metadata = new Metadata();
            metadata.set(Metadata.RESOURCE_NAME_KEY, path);
            AutoDetectParser parser = new AutoDetectParser();
            DefaultHandler handler = new BodyContentHandler();
            parser.parse(in, handler, metadata);
            getDocument().addTokenizedField(FieldNames.CONTENTS,
                    handler.toString());
        } catch (Exception e) {
            LOGGER.error(
                    "We had an exception " + path + " (r"
                    + dirEntry.getRevision() + ")", e);
        } finally {
            try {
                in.close();
            } catch (Exception e) {
                LOGGER.error("We had an exception during closing: ", e);
            }
        }
    }

}
