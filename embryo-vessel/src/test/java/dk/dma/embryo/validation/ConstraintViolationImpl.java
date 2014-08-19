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
