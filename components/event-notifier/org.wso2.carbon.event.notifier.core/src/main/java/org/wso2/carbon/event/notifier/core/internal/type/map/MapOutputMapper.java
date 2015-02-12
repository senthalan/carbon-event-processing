/*
*  Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.event.notifier.core.internal.type.map;

import org.wso2.carbon.databridge.commons.Attribute;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.event.notifier.core.config.EventNotifierConfiguration;
import org.wso2.carbon.event.notifier.core.config.mapping.MapOutputMapping;
import org.wso2.carbon.event.notifier.core.exception.EventNotifierConfigurationException;
import org.wso2.carbon.event.notifier.core.exception.EventNotifierStreamValidationException;
import org.wso2.carbon.event.notifier.core.internal.OutputMapper;
import org.wso2.carbon.event.notifier.core.config.EventOutputProperty;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class MapOutputMapper implements OutputMapper {

    EventNotifierConfiguration eventNotifierConfiguration = null;
    Map<String, Integer> propertyPositionMap = null;
    private int noOfMetaData = 0;
    private int noOfCorrelationData = 0;
    private int noOfPayloadData = 0;
    private StreamDefinition streamDefinition;

    public MapOutputMapper(EventNotifierConfiguration eventNotifierConfiguration,
                           Map<String, Integer> propertyPositionMap,
                           int tenantId, StreamDefinition streamDefinition) throws
            EventNotifierConfigurationException {
        this.eventNotifierConfiguration = eventNotifierConfiguration;
        this.propertyPositionMap = propertyPositionMap;
        if (eventNotifierConfiguration.getOutputMapping().isCustomMappingEnabled()) {
            validateStreamDefinitionWithOutputProperties();
        } else {
            this.streamDefinition = streamDefinition;
            noOfMetaData = streamDefinition.getMetaData() != null ? streamDefinition.getMetaData().size() : 0;
            noOfCorrelationData = streamDefinition.getCorrelationData() != null ? streamDefinition.getCorrelationData().size() : 0;
            noOfPayloadData = streamDefinition.getPayloadData() != null ? streamDefinition.getPayloadData().size() : 0;
        }

    }

    private void validateStreamDefinitionWithOutputProperties()
            throws EventNotifierConfigurationException {

        MapOutputMapping mapOutputMapping = (MapOutputMapping) eventNotifierConfiguration.getOutputMapping();
        List<EventOutputProperty> outputPropertyConfiguration = mapOutputMapping.getOutputPropertyConfiguration();

        Iterator<EventOutputProperty> outputPropertyConfigurationIterator = outputPropertyConfiguration.iterator();
        for (; outputPropertyConfigurationIterator.hasNext(); ) {
            EventOutputProperty outputProperty = outputPropertyConfigurationIterator.next();
            if (!propertyPositionMap.containsKey(outputProperty.getValueOf())) {
                throw new EventNotifierStreamValidationException("Property " + outputProperty.getValueOf() + " is not in the input stream definition. ", streamDefinition.getStreamId());
            }
        }
    }

    @Override
    public Object convertToMappedInputEvent(Object[] eventData)
            throws EventNotifierConfigurationException {
        Map<Object, Object> eventMapObject = new TreeMap<Object, Object>();
        MapOutputMapping mapOutputMapping = (MapOutputMapping) eventNotifierConfiguration.getOutputMapping();
        List<EventOutputProperty> outputPropertyConfiguration = mapOutputMapping.getOutputPropertyConfiguration();

        if (outputPropertyConfiguration.size() != 0 && eventData.length > 0) {
            for (EventOutputProperty eventOutputProperty : outputPropertyConfiguration) {
                int position = propertyPositionMap.get(eventOutputProperty.getValueOf());
                eventMapObject.put(eventOutputProperty.getName(), eventData[position]);
            }
        }
        return eventMapObject;
    }

    @Override
    public Object convertToTypedInputEvent(Object[] eventData) throws EventNotifierConfigurationException {

        Map<Object, Object> eventMapObject = new TreeMap<Object, Object>();
        int counter = 0;

        if (noOfMetaData > 0) {
            for (Attribute metaData : streamDefinition.getMetaData()) {
                eventMapObject.put(metaData.getName(), eventData[counter]);
                counter++;
            }
        }

        if (noOfCorrelationData > 0) {
            for (Attribute correlationData : streamDefinition.getCorrelationData()) {
                eventMapObject.put(correlationData.getName(), eventData[counter]);
                counter++;
            }
        }

        if (noOfPayloadData > 0) {
            for (Attribute payloadData : streamDefinition.getPayloadData()) {
                eventMapObject.put(payloadData.getName(), eventData[counter]);
                counter++;
            }
        }

        return eventMapObject;


    }

}
