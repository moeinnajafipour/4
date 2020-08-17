/*
 * Copyright 1999-2020 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: nacos_grpc_service.proto

package com.alibaba.nacos.api.grpc;

/**
 * Protobuf type {@code GrpcResponse}
 */
public final class GrpcResponse extends com.google.protobuf.GeneratedMessageV3 implements
        // @@protoc_insertion_point(message_implements:GrpcResponse)
        GrpcResponseOrBuilder {
    
    // Use GrpcResponse.newBuilder() to construct.
    private GrpcResponse(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
        super(builder);
    }
    
    private GrpcResponse() {
        code_ = 0;
        ack_ = "";
        type_ = "";
    }
    
    @Override
    public final com.google.protobuf.UnknownFieldSet getUnknownFields() {
        return com.google.protobuf.UnknownFieldSet.getDefaultInstance();
    }
    
    private GrpcResponse(com.google.protobuf.CodedInputStream input,
            com.google.protobuf.ExtensionRegistryLite extensionRegistry)
            throws com.google.protobuf.InvalidProtocolBufferException {
        this();
        int mutable_bitField0_ = 0;
        try {
            boolean done = false;
            while (!done) {
                int tag = input.readTag();
                switch (tag) {
                    case 0:
                        done = true;
                        break;
                    default: {
                        if (!input.skipField(tag)) {
                            done = true;
                        }
                        break;
                    }
                    case 8: {
                        
                        code_ = input.readInt32();
                        break;
                    }
                    case 18: {
                        com.google.protobuf.Any.Builder subBuilder = null;
                        if (body_ != null) {
                            subBuilder = body_.toBuilder();
                        }
                        body_ = input.readMessage(com.google.protobuf.Any.parser(), extensionRegistry);
                        if (subBuilder != null) {
                            subBuilder.mergeFrom(body_);
                            body_ = subBuilder.buildPartial();
                        }
                        
                        break;
                    }
                    case 26: {
                        String s = input.readStringRequireUtf8();
                        
                        type_ = s;
                        break;
                    }
                    case 34: {
                        String s = input.readStringRequireUtf8();
                        
                        ack_ = s;
                        break;
                    }
                }
            }
        } catch (com.google.protobuf.InvalidProtocolBufferException e) {
            throw e.setUnfinishedMessage(this);
        } catch (java.io.IOException e) {
            throw new com.google.protobuf.InvalidProtocolBufferException(e).setUnfinishedMessage(this);
        } finally {
            makeExtensionsImmutable();
        }
    }
    
    public static final com.google.protobuf.Descriptors.Descriptor getDescriptor() {
        return NacosGrpcService.internal_static_GrpcResponse_descriptor;
    }
    
    protected FieldAccessorTable internalGetFieldAccessorTable() {
        return NacosGrpcService.internal_static_GrpcResponse_fieldAccessorTable
                .ensureFieldAccessorsInitialized(GrpcResponse.class, GrpcResponse.Builder.class);
    }
    
    public static final int CODE_FIELD_NUMBER = 1;
    
    private int code_;
    
    /**
     * <code>int32 code = 1;</code>
     */
    public int getCode() {
        return code_;
    }
    
    public static final int BODY_FIELD_NUMBER = 2;
    
    private com.google.protobuf.Any body_;
    
    /**
     * <pre>
     * reponse body
     * </pre>
     *
     * <code>.google.protobuf.Any body = 2;</code>
     */
    public boolean hasBody() {
        return body_ != null;
    }
    
    /**
     * <pre>
     * reponse body
     * </pre>
     *
     * <code>.google.protobuf.Any body = 2;</code>
     */
    public com.google.protobuf.Any getBody() {
        return body_ == null ? com.google.protobuf.Any.getDefaultInstance() : body_;
    }
    
    /**
     * <pre>
     * reponse body
     * </pre>
     *
     * <code>.google.protobuf.Any body = 2;</code>
     */
    public com.google.protobuf.AnyOrBuilder getBodyOrBuilder() {
        return getBody();
    }
    
    public static final int ACK_FIELD_NUMBER = 4;
    
    private volatile Object ack_;
    
    /**
     * <code>string ack = 4;</code>
     */
    public String getAck() {
        Object ref = ack_;
        if (ref instanceof String) {
            return (String) ref;
        } else {
            com.google.protobuf.ByteString bs = (com.google.protobuf.ByteString) ref;
            String s = bs.toStringUtf8();
            ack_ = s;
            return s;
        }
    }
    
    /**
     * <code>string ack = 4;</code>
     */
    public com.google.protobuf.ByteString getAckBytes() {
        Object ref = ack_;
        if (ref instanceof String) {
            com.google.protobuf.ByteString b = com.google.protobuf.ByteString.copyFromUtf8((String) ref);
            ack_ = b;
            return b;
        } else {
            return (com.google.protobuf.ByteString) ref;
        }
    }
    
    public static final int TYPE_FIELD_NUMBER = 3;
    
    private volatile Object type_;
    
    /**
     * <code>string type = 3;</code>
     */
    public String getType() {
        Object ref = type_;
        if (ref instanceof String) {
            return (String) ref;
        } else {
            com.google.protobuf.ByteString bs = (com.google.protobuf.ByteString) ref;
            String s = bs.toStringUtf8();
            type_ = s;
            return s;
        }
    }
    
    /**
     * <code>string type = 3;</code>
     */
    public com.google.protobuf.ByteString getTypeBytes() {
        Object ref = type_;
        if (ref instanceof String) {
            com.google.protobuf.ByteString b = com.google.protobuf.ByteString.copyFromUtf8((String) ref);
            type_ = b;
            return b;
        } else {
            return (com.google.protobuf.ByteString) ref;
        }
    }
    
    private byte memoizedIsInitialized = -1;
    
    public final boolean isInitialized() {
        byte isInitialized = memoizedIsInitialized;
        if (isInitialized == 1) {
            return true;
        }
        if (isInitialized == 0) {
            return false;
        }
        
        memoizedIsInitialized = 1;
        return true;
    }
    
    public void writeTo(com.google.protobuf.CodedOutputStream output) throws java.io.IOException {
        if (code_ != 0) {
            output.writeInt32(1, code_);
        }
        if (body_ != null) {
            output.writeMessage(2, getBody());
        }
        if (!getTypeBytes().isEmpty()) {
            com.google.protobuf.GeneratedMessageV3.writeString(output, 3, type_);
        }
        if (!getAckBytes().isEmpty()) {
            com.google.protobuf.GeneratedMessageV3.writeString(output, 4, ack_);
        }
    }
    
    public int getSerializedSize() {
        int size = memoizedSize;
        if (size != -1) {
            return size;
        }
        
        size = 0;
        if (code_ != 0) {
            size += com.google.protobuf.CodedOutputStream.computeInt32Size(1, code_);
        }
        if (body_ != null) {
            size += com.google.protobuf.CodedOutputStream.computeMessageSize(2, getBody());
        }
        if (!getTypeBytes().isEmpty()) {
            size += com.google.protobuf.GeneratedMessageV3.computeStringSize(3, type_);
        }
        if (!getAckBytes().isEmpty()) {
            size += com.google.protobuf.GeneratedMessageV3.computeStringSize(4, ack_);
        }
        memoizedSize = size;
        return size;
    }
    
    private static final long serialVersionUID = 0L;
    
    @Override
    public boolean equals(final Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof GrpcResponse)) {
            return super.equals(obj);
        }
        GrpcResponse other = (GrpcResponse) obj;
        
        boolean result = true;
        result = result && (getCode() == other.getCode());
        result = result && (hasBody() == other.hasBody());
        if (hasBody()) {
            result = result && getBody().equals(other.getBody());
        }
        result = result && getAck().equals(other.getAck());
        result = result && getType().equals(other.getType());
        return result;
    }
    
    @Override
    public int hashCode() {
        if (memoizedHashCode != 0) {
            return memoizedHashCode;
        }
        int hash = 41;
        hash = (19 * hash) + getDescriptor().hashCode();
        hash = (37 * hash) + CODE_FIELD_NUMBER;
        hash = (53 * hash) + getCode();
        if (hasBody()) {
            hash = (37 * hash) + BODY_FIELD_NUMBER;
            hash = (53 * hash) + getBody().hashCode();
        }
        hash = (37 * hash) + ACK_FIELD_NUMBER;
        hash = (53 * hash) + getAck().hashCode();
        hash = (37 * hash) + TYPE_FIELD_NUMBER;
        hash = (53 * hash) + getType().hashCode();
        hash = (29 * hash) + unknownFields.hashCode();
        memoizedHashCode = hash;
        return hash;
    }
    
    public static GrpcResponse parseFrom(java.nio.ByteBuffer data)
            throws com.google.protobuf.InvalidProtocolBufferException {
        return PARSER.parseFrom(data);
    }
    
    public static GrpcResponse parseFrom(java.nio.ByteBuffer data,
            com.google.protobuf.ExtensionRegistryLite extensionRegistry)
            throws com.google.protobuf.InvalidProtocolBufferException {
        return PARSER.parseFrom(data, extensionRegistry);
    }
    
    public static GrpcResponse parseFrom(com.google.protobuf.ByteString data)
            throws com.google.protobuf.InvalidProtocolBufferException {
        return PARSER.parseFrom(data);
    }
    
    public static GrpcResponse parseFrom(com.google.protobuf.ByteString data,
            com.google.protobuf.ExtensionRegistryLite extensionRegistry)
            throws com.google.protobuf.InvalidProtocolBufferException {
        return PARSER.parseFrom(data, extensionRegistry);
    }
    
    public static GrpcResponse parseFrom(byte[] data) throws com.google.protobuf.InvalidProtocolBufferException {
        return PARSER.parseFrom(data);
    }
    
    public static GrpcResponse parseFrom(byte[] data, com.google.protobuf.ExtensionRegistryLite extensionRegistry)
            throws com.google.protobuf.InvalidProtocolBufferException {
        return PARSER.parseFrom(data, extensionRegistry);
    }
    
    public static GrpcResponse parseFrom(java.io.InputStream input) throws java.io.IOException {
        return com.google.protobuf.GeneratedMessageV3.parseWithIOException(PARSER, input);
    }
    
    public static GrpcResponse parseFrom(java.io.InputStream input,
            com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
        return com.google.protobuf.GeneratedMessageV3.parseWithIOException(PARSER, input, extensionRegistry);
    }
    
    public static GrpcResponse parseDelimitedFrom(java.io.InputStream input) throws java.io.IOException {
        return com.google.protobuf.GeneratedMessageV3.parseDelimitedWithIOException(PARSER, input);
    }
    
    public static GrpcResponse parseDelimitedFrom(java.io.InputStream input,
            com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
        return com.google.protobuf.GeneratedMessageV3.parseDelimitedWithIOException(PARSER, input, extensionRegistry);
    }
    
    public static GrpcResponse parseFrom(com.google.protobuf.CodedInputStream input) throws java.io.IOException {
        return com.google.protobuf.GeneratedMessageV3.parseWithIOException(PARSER, input);
    }
    
    public static GrpcResponse parseFrom(com.google.protobuf.CodedInputStream input,
            com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
        return com.google.protobuf.GeneratedMessageV3.parseWithIOException(PARSER, input, extensionRegistry);
    }
    
    public Builder newBuilderForType() {
        return newBuilder();
    }
    
    public static Builder newBuilder() {
        return DEFAULT_INSTANCE.toBuilder();
    }
    
    public static Builder newBuilder(GrpcResponse prototype) {
        return DEFAULT_INSTANCE.toBuilder().mergeFrom(prototype);
    }
    
    public Builder toBuilder() {
        return this == DEFAULT_INSTANCE ? new Builder() : new Builder().mergeFrom(this);
    }
    
    @Override
    protected Builder newBuilderForType(BuilderParent parent) {
        Builder builder = new Builder(parent);
        return builder;
    }
    
    /**
     * Protobuf type {@code GrpcResponse}
     */
    public static final class Builder extends com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
            // @@protoc_insertion_point(builder_implements:GrpcResponse)
            GrpcResponseOrBuilder {
        
        public static final com.google.protobuf.Descriptors.Descriptor getDescriptor() {
            return NacosGrpcService.internal_static_GrpcResponse_descriptor;
        }
        
        protected FieldAccessorTable internalGetFieldAccessorTable() {
            return NacosGrpcService.internal_static_GrpcResponse_fieldAccessorTable
                    .ensureFieldAccessorsInitialized(GrpcResponse.class, GrpcResponse.Builder.class);
        }
        
        // Construct using com.alibaba.nacos.api.grpc.GrpcResponse.newBuilder()
        private Builder() {
            maybeForceBuilderInitialization();
        }
        
        private Builder(BuilderParent parent) {
            super(parent);
            maybeForceBuilderInitialization();
        }
        
        private void maybeForceBuilderInitialization() {
            if (com.google.protobuf.GeneratedMessageV3.alwaysUseFieldBuilders) {
            }
        }
        
        public Builder clear() {
            super.clear();
            code_ = 0;
            
            if (bodyBuilder_ == null) {
                body_ = null;
            } else {
                body_ = null;
                bodyBuilder_ = null;
            }
            ack_ = "";
            
            type_ = "";
            
            return this;
        }
        
        public com.google.protobuf.Descriptors.Descriptor getDescriptorForType() {
            return NacosGrpcService.internal_static_GrpcResponse_descriptor;
        }
        
        public GrpcResponse getDefaultInstanceForType() {
            return GrpcResponse.getDefaultInstance();
        }
        
        public GrpcResponse build() {
            GrpcResponse result = buildPartial();
            if (!result.isInitialized()) {
                throw newUninitializedMessageException(result);
            }
            return result;
        }
        
        public GrpcResponse buildPartial() {
            GrpcResponse result = new GrpcResponse(this);
            result.code_ = code_;
            if (bodyBuilder_ == null) {
                result.body_ = body_;
            } else {
                result.body_ = bodyBuilder_.build();
            }
            result.ack_ = ack_;
            result.type_ = type_;
            onBuilt();
            return result;
        }
        
        public Builder clone() {
            return (Builder) super.clone();
        }
        
        public Builder setField(com.google.protobuf.Descriptors.FieldDescriptor field, Object value) {
            return (Builder) super.setField(field, value);
        }
        
        public Builder clearField(com.google.protobuf.Descriptors.FieldDescriptor field) {
            return (Builder) super.clearField(field);
        }
        
        public Builder clearOneof(com.google.protobuf.Descriptors.OneofDescriptor oneof) {
            return (Builder) super.clearOneof(oneof);
        }
        
        public Builder setRepeatedField(com.google.protobuf.Descriptors.FieldDescriptor field, int index,
                Object value) {
            return (Builder) super.setRepeatedField(field, index, value);
        }
        
        public Builder addRepeatedField(com.google.protobuf.Descriptors.FieldDescriptor field, Object value) {
            return (Builder) super.addRepeatedField(field, value);
        }
        
        public Builder mergeFrom(com.google.protobuf.Message other) {
            if (other instanceof GrpcResponse) {
                return mergeFrom((GrpcResponse) other);
            } else {
                super.mergeFrom(other);
                return this;
            }
        }
        
        public Builder mergeFrom(GrpcResponse other) {
            if (other == GrpcResponse.getDefaultInstance()) {
                return this;
            }
            if (other.getCode() != 0) {
                setCode(other.getCode());
            }
            if (other.hasBody()) {
                mergeBody(other.getBody());
            }
            if (!other.getAck().isEmpty()) {
                ack_ = other.ack_;
                onChanged();
            }
            if (!other.getType().isEmpty()) {
                type_ = other.type_;
                onChanged();
            }
            onChanged();
            return this;
        }
        
        public final boolean isInitialized() {
            return true;
        }
        
        public Builder mergeFrom(com.google.protobuf.CodedInputStream input,
                com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
            GrpcResponse parsedMessage = null;
            try {
                parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
            } catch (com.google.protobuf.InvalidProtocolBufferException e) {
                parsedMessage = (GrpcResponse) e.getUnfinishedMessage();
                throw e.unwrapIOException();
            } finally {
                if (parsedMessage != null) {
                    mergeFrom(parsedMessage);
                }
            }
            return this;
        }
        
        private int code_;
        
        /**
         * <code>int32 code = 1;</code>
         */
        public int getCode() {
            return code_;
        }
        
        /**
         * <code>int32 code = 1;</code>
         */
        public Builder setCode(int value) {
            
            code_ = value;
            onChanged();
            return this;
        }
        
        /**
         * <code>int32 code = 1;</code>
         */
        public Builder clearCode() {
            
            code_ = 0;
            onChanged();
            return this;
        }
        
        private com.google.protobuf.Any body_ = null;
        
        private com.google.protobuf.SingleFieldBuilderV3<com.google.protobuf.Any, com.google.protobuf.Any.Builder, com.google.protobuf.AnyOrBuilder> bodyBuilder_;
        
        /**
         * <pre>
         * reponse body
         * </pre>
         *
         * <code>.google.protobuf.Any body = 2;</code>
         */
        public boolean hasBody() {
            return bodyBuilder_ != null || body_ != null;
        }
        
        /**
         * <pre>
         * reponse body
         * </pre>
         *
         * <code>.google.protobuf.Any body = 2;</code>
         */
        public com.google.protobuf.Any getBody() {
            if (bodyBuilder_ == null) {
                return body_ == null ? com.google.protobuf.Any.getDefaultInstance() : body_;
            } else {
                return bodyBuilder_.getMessage();
            }
        }
        
        /**
         * <pre>
         * reponse body
         * </pre>
         *
         * <code>.google.protobuf.Any body = 2;</code>
         */
        public Builder setBody(com.google.protobuf.Any value) {
            if (bodyBuilder_ == null) {
                if (value == null) {
                    throw new NullPointerException();
                }
                body_ = value;
                onChanged();
            } else {
                bodyBuilder_.setMessage(value);
            }
            
            return this;
        }
        
        /**
         * <pre>
         * reponse body
         * </pre>
         *
         * <code>.google.protobuf.Any body = 2;</code>
         */
        public Builder setBody(com.google.protobuf.Any.Builder builderForValue) {
            if (bodyBuilder_ == null) {
                body_ = builderForValue.build();
                onChanged();
            } else {
                bodyBuilder_.setMessage(builderForValue.build());
            }
            
            return this;
        }
        
        /**
         * <pre>
         * reponse body
         * </pre>
         *
         * <code>.google.protobuf.Any body = 2;</code>
         */
        public Builder mergeBody(com.google.protobuf.Any value) {
            if (bodyBuilder_ == null) {
                if (body_ != null) {
                    body_ = com.google.protobuf.Any.newBuilder(body_).mergeFrom(value).buildPartial();
                } else {
                    body_ = value;
                }
                onChanged();
            } else {
                bodyBuilder_.mergeFrom(value);
            }
            
            return this;
        }
        
        /**
         * <pre>
         * reponse body
         * </pre>
         *
         * <code>.google.protobuf.Any body = 2;</code>
         */
        public Builder clearBody() {
            if (bodyBuilder_ == null) {
                body_ = null;
                onChanged();
            } else {
                body_ = null;
                bodyBuilder_ = null;
            }
            
            return this;
        }
        
        /**
         * <pre>
         * reponse body
         * </pre>
         *
         * <code>.google.protobuf.Any body = 2;</code>
         */
        public com.google.protobuf.Any.Builder getBodyBuilder() {
            
            onChanged();
            return getBodyFieldBuilder().getBuilder();
        }
        
        /**
         * <pre>
         * reponse body
         * </pre>
         *
         * <code>.google.protobuf.Any body = 2;</code>
         */
        public com.google.protobuf.AnyOrBuilder getBodyOrBuilder() {
            if (bodyBuilder_ != null) {
                return bodyBuilder_.getMessageOrBuilder();
            } else {
                return body_ == null ? com.google.protobuf.Any.getDefaultInstance() : body_;
            }
        }
        
        /**
         * <pre>
         * reponse body
         * </pre>
         *
         * <code>.google.protobuf.Any body = 2;</code>
         */
        private com.google.protobuf.SingleFieldBuilderV3<com.google.protobuf.Any, com.google.protobuf.Any.Builder, com.google.protobuf.AnyOrBuilder> getBodyFieldBuilder() {
            if (bodyBuilder_ == null) {
                bodyBuilder_ = new com.google.protobuf.SingleFieldBuilderV3<com.google.protobuf.Any, com.google.protobuf.Any.Builder, com.google.protobuf.AnyOrBuilder>(
                        getBody(), getParentForChildren(), isClean());
                body_ = null;
            }
            return bodyBuilder_;
        }
        
        private Object ack_ = "";
        
        /**
         * <code>string ack = 4;</code>
         */
        public String getAck() {
            Object ref = ack_;
            if (!(ref instanceof String)) {
                com.google.protobuf.ByteString bs = (com.google.protobuf.ByteString) ref;
                String s = bs.toStringUtf8();
                ack_ = s;
                return s;
            } else {
                return (String) ref;
            }
        }
        
        /**
         * <code>string ack = 4;</code>
         */
        public com.google.protobuf.ByteString getAckBytes() {
            Object ref = ack_;
            if (ref instanceof String) {
                com.google.protobuf.ByteString b = com.google.protobuf.ByteString.copyFromUtf8((String) ref);
                ack_ = b;
                return b;
            } else {
                return (com.google.protobuf.ByteString) ref;
            }
        }
        
        /**
         * <code>string ack = 4;</code>
         */
        public Builder setAck(String value) {
            if (value == null) {
                throw new NullPointerException();
            }
            
            ack_ = value;
            onChanged();
            return this;
        }
        
        /**
         * <code>string ack = 4;</code>
         */
        public Builder clearAck() {
            
            ack_ = getDefaultInstance().getAck();
            onChanged();
            return this;
        }
        
        /**
         * <code>string ack = 4;</code>
         */
        public Builder setAckBytes(com.google.protobuf.ByteString value) {
            if (value == null) {
                throw new NullPointerException();
            }
            checkByteStringIsUtf8(value);
            
            ack_ = value;
            onChanged();
            return this;
        }
        
        private Object type_ = "";
        
        /**
         * <code>string type = 3;</code>
         */
        public String getType() {
            Object ref = type_;
            if (!(ref instanceof String)) {
                com.google.protobuf.ByteString bs = (com.google.protobuf.ByteString) ref;
                String s = bs.toStringUtf8();
                type_ = s;
                return s;
            } else {
                return (String) ref;
            }
        }
        
        /**
         * <code>string type = 3;</code>
         */
        public com.google.protobuf.ByteString getTypeBytes() {
            Object ref = type_;
            if (ref instanceof String) {
                com.google.protobuf.ByteString b = com.google.protobuf.ByteString.copyFromUtf8((String) ref);
                type_ = b;
                return b;
            } else {
                return (com.google.protobuf.ByteString) ref;
            }
        }
        
        /**
         * <code>string type = 3;</code>
         */
        public Builder setType(String value) {
            if (value == null) {
                throw new NullPointerException();
            }
            
            type_ = value;
            onChanged();
            return this;
        }
        
        /**
         * <code>string type = 3;</code>
         */
        public Builder clearType() {
            
            type_ = getDefaultInstance().getType();
            onChanged();
            return this;
        }
        
        /**
         * <code>string type = 3;</code>
         */
        public Builder setTypeBytes(com.google.protobuf.ByteString value) {
            if (value == null) {
                throw new NullPointerException();
            }
            checkByteStringIsUtf8(value);
            
            type_ = value;
            onChanged();
            return this;
        }
        
        public final Builder setUnknownFields(final com.google.protobuf.UnknownFieldSet unknownFields) {
            return this;
        }
        
        public final Builder mergeUnknownFields(final com.google.protobuf.UnknownFieldSet unknownFields) {
            return this;
        }
        
        // @@protoc_insertion_point(builder_scope:GrpcResponse)
    }
    
    // @@protoc_insertion_point(class_scope:GrpcResponse)
    private static final GrpcResponse DEFAULT_INSTANCE;
    
    static {
        DEFAULT_INSTANCE = new GrpcResponse();
    }
    
    public static GrpcResponse getDefaultInstance() {
        return DEFAULT_INSTANCE;
    }
    
    private static final com.google.protobuf.Parser<GrpcResponse> PARSER = new com.google.protobuf.AbstractParser<GrpcResponse>() {
        public GrpcResponse parsePartialFrom(com.google.protobuf.CodedInputStream input,
                com.google.protobuf.ExtensionRegistryLite extensionRegistry)
                throws com.google.protobuf.InvalidProtocolBufferException {
            return new GrpcResponse(input, extensionRegistry);
        }
    };
    
    public static com.google.protobuf.Parser<GrpcResponse> parser() {
        return PARSER;
    }
    
    @Override
    public com.google.protobuf.Parser<GrpcResponse> getParserForType() {
        return PARSER;
    }
    
    public GrpcResponse getDefaultInstanceForType() {
        return DEFAULT_INSTANCE;
    }
    
}

