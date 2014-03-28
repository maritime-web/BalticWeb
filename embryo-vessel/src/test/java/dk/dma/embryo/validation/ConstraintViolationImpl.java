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

import java.util.HashSet;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Path;
import javax.validation.metadata.ConstraintDescriptor;


public class ConstraintViolationImpl implements ConstraintViolation {

    private Object invalidValue;
    private String message;
    private Path propertyPath;

    public static Set<ConstraintViolation<?>> fromOtherProvider(Set<ConstraintViolation<?>> violations) {
        Set<ConstraintViolation<?>> transformed = new HashSet<>();
        for (ConstraintViolation<?> violation : violations) {
            transformed.add(ConstraintViolationImpl.fromOtherProvider(violation));
        }
        return transformed;
    }

    public static ConstraintViolation<?> fromOtherProvider(ConstraintViolation<?> violation) {
        return new ConstraintViolationImpl(violation.getPropertyPath().toString(), violation.getInvalidValue(),
                violation.getMessage());
    }

    public ConstraintViolationImpl(String propertyPath, Object invalidValue, String message) {
        super();
        this.invalidValue = invalidValue;
        this.message = message;
        this.propertyPath = new PathImpl(propertyPath);
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public String getMessageTemplate() {
        return null;
    }

    @Override
    public Object getRootBean() {
        return null;
    }

    @Override
    public Class getRootBeanClass() {
        return null;
    }

    @Override
    public Object getLeafBean() {
        return null;
    }

    @Override
    public Path getPropertyPath() {
        return propertyPath;
    }

    @Override
    public Object getInvalidValue() {
        return invalidValue;
    }

    @Override
    public ConstraintDescriptor getConstraintDescriptor() {
        return null;
    }

}
