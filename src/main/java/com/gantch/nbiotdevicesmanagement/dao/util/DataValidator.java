/**
 * Copyright © 2016-2017 The Thingsboard Authors
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
package com.gantch.nbiotdevicesmanagement.dao.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.gantch.nbiotdevicesmanagement.dao.BaseData;
import com.gantch.nbiotdevicesmanagement.dao.exception.DataValidationException;
import lombok.extern.slf4j.Slf4j;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * 数据验证
 * @param <D>
 */
@Slf4j
public abstract class DataValidator<D extends BaseData> {

    
    public void validate(D data) {
        try {
            if (data == null) {
                throw new DataValidationException("Data object can't be null!");
            }
            validateDataImpl(data);
            if (data.getId() == null) {
                validateCreate(data);
            } else {
                validateUpdate(data);
            }
        } catch (DataValidationException e) {throw e;
        }
    }
    
    protected void validateDataImpl(D data) {
    }
    
    protected void validateCreate(D data) {
    }

    protected void validateUpdate(D data) {
    }
    
    protected boolean isSameData(D existentData, D actualData) {
        return actualData.getId() != null && existentData.getId().equals(actualData.getId());
    }
    
    protected static void validateJsonStructure(JsonNode expectedNode, JsonNode actualNode) {
        Set<String> expectedFields = new HashSet<>();        
        Iterator<String> fieldsIterator = expectedNode.fieldNames();
        while (fieldsIterator.hasNext()) {
            expectedFields.add(fieldsIterator.next());
        }
        
        Set<String> actualFields = new HashSet<>();        
        fieldsIterator = actualNode.fieldNames();
        while (fieldsIterator.hasNext()) {
            actualFields.add(fieldsIterator.next());
        }
        
        if (!expectedFields.containsAll(actualFields) || !actualFields.containsAll(expectedFields)) {
            throw new DataValidationException("Provided json structure is different from stored one '" + actualNode + "'!");
        }
        
        for (String field : actualFields) {
            if (!actualNode.get(field).isTextual()) {
                throw new DataValidationException("Provided json structure can't contain non-text values '" + actualNode + "'!");
            }
        }
    }
}
