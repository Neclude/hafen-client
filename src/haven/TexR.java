/*
 *  This file is part of the Haven & Hearth game client.
 *  Copyright (C) 2009 Fredrik Tolf <fredrik@dolda2000.com>, and
 *                     Björn Johannessen <johannessen.bjorn@gmail.com>
 *
 *  Redistribution and/or modification of this file is subject to the
 *  terms of the GNU Lesser General Public License, version 3, as
 *  published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  Other parts of this source tree adhere to other copying
 *  rights. Please see the file `COPYING' in the root directory of the
 *  source tree for details.
 *
 *  A copy the GNU Lesser General Public License is distributed along
 *  with the source tree of which this file is a part in the file
 *  `doc/LPGL-3'. If it is missing for any reason, please see the Free
 *  Software Foundation's website at <http://www.fsf.org/>, or write
 *  to the Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 *  Boston, MA 02111-1307 USA
 */

package haven;

import java.util.*;
import java.awt.image.*;
import java.io.*;
import javax.imageio.ImageIO;
import java.awt.color.ColorSpace;
import java.nio.ByteBuffer;
import javax.media.opengl.*;
import haven.Defer.Future;

@Resource.LayerName("tex")
public class TexR extends Resource.Layer implements Resource.IDLayer<Integer> {
    transient private byte[] img;
    transient private final TexL tex;
    private final Coord off, sz;
    public final int id;

    public TexR(Resource res, byte[] rbuf) {
	res.super();
	Message buf = new Message(0, rbuf);
	this.id = buf.int16();
	this.off = new Coord(buf.uint16(), buf.uint16());
	this.sz = new Coord(buf.uint16(), buf.uint16());
	this.tex = new Real();
	int minfilter = -1, magfilter = -1;
	while(!buf.eom()) {
	    int t = buf.uint8();
	    switch(t) {
	    case 0:
		this.img = buf.bytes(buf.int32());
		break;
	    case 1:
		int ma = buf.uint8();
		tex.mipmap(new Mipmapper[] {
			Mipmapper.avg, // Default
			Mipmapper.avg, // Specific
			Mipmapper.rnd,
		    }[ma]);
		break;
	    case 2:
		int magf = buf.uint8();
		magfilter = new int[] {GL.GL_NEAREST, GL.GL_LINEAR}[magf];
		break;
	    case 3:
		int minf = buf.uint8();
		minfilter = new int[] {GL.GL_NEAREST, GL.GL_LINEAR,
				       GL.GL_NEAREST_MIPMAP_NEAREST, GL.GL_NEAREST_MIPMAP_LINEAR,
				       GL.GL_LINEAR_MIPMAP_NEAREST, GL.GL_LINEAR_MIPMAP_LINEAR,
		}[minf];
		break;
	    default:
		throw(new Resource.LoadException("Unknown texture data part " + t + " in " + res.name, getres()));
	    }
	}
	if(magfilter == -1)
	    magfilter = GL.GL_LINEAR;
	if(minfilter == -1)
	    minfilter = (tex.mipmap == null)?GL.GL_LINEAR:GL.GL_LINEAR_MIPMAP_LINEAR;
	tex.magfilter(magfilter);
	tex.minfilter(minfilter);
    }

    private class Real extends TexL {
	private Real() {
	    super(sz);
	}

	protected BufferedImage fill() {
	    try {
		return(ImageIO.read(new ByteArrayInputStream(TexR.this.img)));
	    } catch(IOException e) {
		throw(new RuntimeException("Invalid image data in " + getres().name, e));
	    }
	}

	public String toString() {
	    return("TexR(" + getres().name + ")");
	}
    }

    public Tex tex() {
	return(tex);
    }

    public Integer layerid() {
	return(id);
    }

    public void init() {}
}