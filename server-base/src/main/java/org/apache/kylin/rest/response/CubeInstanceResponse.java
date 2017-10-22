/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package org.apache.kylin.rest.response;

import org.apache.kylin.cube.CubeInstance;
import org.apache.kylin.cube.CubeSegment;
import org.apache.kylin.metadata.model.ISourceAware;
import org.apache.kylin.metadata.model.SegmentStatusEnum;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 */
@SuppressWarnings("serial")
public class CubeInstanceResponse extends CubeInstance {

    @JsonProperty("project")
    private String project;
    @JsonProperty("model")
    private String model;
    @JsonProperty("is_streaming")
    private boolean isStreaming;
    @JsonProperty("partitionDateColumn")
    private String partitionDateColumn;
    @JsonProperty("partitionDateStart")
    private long partitionDateStart;
    @JsonProperty("size_kb")
    private long sizeKB;
    @JsonProperty("input_records_count")
    private long inputRecordCnt;
    @JsonProperty("input_records_size")
    private long inputRecordSizeMB;

    public CubeInstanceResponse(CubeInstance cube, String project) {

        this.project = project;

        if (cube == null)
            return;

        setUuid(cube.getUuid());
        setVersion(cube.getVersion());
        setName(cube.getName());
        setOwner(cube.getOwner());
        setDescName(cube.getDescName());
        setCost(cube.getCost());
        setStatus(cube.getStatus());
        setSegments(cube.getSegments());
        setCreateTimeUTC(cube.getCreateTimeUTC());
        setLastModified(cube.getLastModified());

        this.model = cube.getDescriptor().getModelName();
        this.partitionDateStart = cube.getDescriptor().getPartitionDateStart();
        // cuz model doesn't have a state to label a model is broken,
        // so in some case the model can not be loaded due to some check failed,
        // but the cube in this model can still be loaded.
        if (cube.getModel() != null) {
            this.partitionDateColumn = cube.getModel().getPartitionDesc().getPartitionDateColumn();
            this.isStreaming = cube.getModel().getRootFactTable().getTableDesc()
                    .getSourceType() == ISourceAware.ID_STREAMING;
        }

        initSizeKB();
        initInputRecordCount();
        initInputRecordSizeMB();
    }

    protected void setModel(String model) {
        this.model = model;
    }

    protected void initSizeKB() {
        long sizeKb = 0L;

        for (CubeSegment cubeSegment : this.getSegments(SegmentStatusEnum.READY)) {
            sizeKb += cubeSegment.getSizeKB();
        }

        this.sizeKB = sizeKb;
    }

    protected void initInputRecordCount() {
        long inputRecordCount = 0L;

        for (CubeSegment cubeSegment : this.getSegments(SegmentStatusEnum.READY)) {
            inputRecordCount += cubeSegment.getInputRecords();
        }

        this.inputRecordCnt = inputRecordCount;
    }

    protected void initInputRecordSizeMB() {
        long inputRecordSize = 0L;

        for (CubeSegment cubeSegment : this.getSegments(SegmentStatusEnum.READY)) {
            inputRecordSize += cubeSegment.getInputRecordsSize();
        }

        this.inputRecordSizeMB = inputRecordSize;
    }

}