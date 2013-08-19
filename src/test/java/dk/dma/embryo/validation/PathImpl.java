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
package dk.dma.embryo.validation;

import java.util.Iterator;
import java.util.LinkedList;

import javax.validation.Path;

public class PathImpl implements Path {

    private Node path;

    public PathImpl(String path) {
        this.path = new NodeImpl(path);
    }

    @Override
    public Iterator<Node> iterator() {
        LinkedList<Node> l = new LinkedList<>();
        l.add(path);
        return l.iterator();
    }

    static class NodeImpl implements Node {

        private String name;

        public NodeImpl(String name) {
            this.name = name;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public boolean isInIterable() {
            return false;
        }

        @Override
        public Integer getIndex() {
            return null;
        }

        @Override
        public Object getKey() {
            return null;
        }

    }

}
