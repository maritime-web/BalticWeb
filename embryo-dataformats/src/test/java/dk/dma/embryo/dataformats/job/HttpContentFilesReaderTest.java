/* Copyright (c) 2011 Danish Maritime Authority
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library.  If not, see <http://www.gnu.org/licenses/>.
 */
package dk.dma.embryo.dataformats.job;

import java.io.IOException;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Assert;
import org.junit.Test;
import org.unitils.reflectionassert.ReflectionAssert;

import dk.dma.embryo.dataformats.job.HttpContentFilesReader;

/**
 * @author Jesper Tejlgaard
 */
public class HttpContentFilesReaderTest {

    @Test
    public void test() throws IOException {
        String xml = "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 3.2 Final//EN\" \"lkdj\">";
        xml += "<html>";
        xml += " <head>";
        xml += "  <title>Index of /datasets/d0004/chu/sigrid/2014</title>";
        xml += "  <link rel='stylesheet' href='/css/main.css' type='text/css'>";
        xml += "  <META NAME='ROBOTS' CONTENT='NOINDEX,NOFOLLOW'>";
        xml += " </head>";
        xml += " <body>";
        xml += "  <h1>Index of /datasets/d0004/chu/sigrid/2014</h1>";
        xml += "  <table>";
        xml += "   <tr><th><img src='/icons/blank.gif' alt='[ICO]'></th><th><a href='?C=N;O=D'>Name</a></th><th><a href='?C=M;O=A'>Last modified</a></th><th><a href='?C=S;O=A'>Size</a></th><th><a href='?C=D;O=A'>Description</a></th></tr><tr><th colspan='5'><hr></th></tr>";
        xml += "   <tr><td valign='top'><img src='/icons/back.gif' alt='[DIR]'></td><td><a href='/datasets/d0004/chu/sigrid/'>Parent Directory</a></td><td>&nbsp;</td><td align='right'>  - </td><td>&nbsp;</td></tr>";
        xml += "   <tr><td valign='top'><img src='/icons/unknown.gif' alt='[   ]'></td><td><a href='aari_chu_20140107_pl_a.dbf'>aari_chu_20140107_pl_a.dbf</a></td><td align='right'>22-Jan-2014 16:54  </td><td align='right'>5.1K</td><td>&nbsp;</td></tr>";
        xml += "   <tr><td valign='top'><img src='/icons/unknown.gif' alt='[   ]'></td><td><a href='aari_chu_20140107_pl_a.prj'>aari_chu_20140107_pl_a.prj</a></td><td align='right'>07-Jan-2014 13:30  </td><td align='right'>145 </td><td>&nbsp;</td></tr>";
        xml += "   <tr><td valign='top'><img src='/icons/unknown.gif' alt='[   ]'></td><td><a href='aari_chu_20140107_pl_a.shp'>aari_chu_20140107_pl_a.shp</a></td><td align='right'>07-Jan-2014 13:30  </td><td align='right'>133K</td><td>&nbsp;</td></tr>";
        xml += "   <tr><td valign='top'><img src='/icons/unknown.gif' alt='[   ]'></td><td><a href='aari_chu_20140107_pl_a.shx'>aari_chu_20140107_pl_a.shx</a></td><td align='right'>07-Jan-2014 13:30  </td><td align='right'>404 </td><td>&nbsp;</td></tr>";
        xml += "   <tr><td valign='top'><img src='/icons/compressed.gif' alt='[   ]'></td><td><a href='aari_chu_20140107_pl_a.zip'>aari_chu_20140107_pl_a.zip</a></td><td align='right'>22-Jan-2014 16:54  </td><td align='right'>113K</td><td>&nbsp;</td></tr>";
        xml += "   <tr><th colspan='5'><hr></th></tr>";
        xml += "  </table>";
        xml += " </body>";
        xml += "</html>";

        Document doc = Jsoup.parse(xml);

        HttpContentFilesReader contentReader = new HttpContentFilesReader();

        List<String> files = contentReader.readFiles(doc);

        Assert.assertNotNull(files);
        ReflectionAssert.assertReflectionEquals(new String[] { "aari_chu_20140107_pl_a.dbf",
                "aari_chu_20140107_pl_a.prj", "aari_chu_20140107_pl_a.shp", "aari_chu_20140107_pl_a.shx" }, files.toArray());
    }

}
