/* Copyright (c) 2011 Danish Maritime Authority.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
