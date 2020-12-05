// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: mcp.proto

package com.alibaba.nacos.istio.model.mcp;

public interface IncrementalMeshConfigResponseOrBuilder extends
    // @@protoc_insertion_point(interface_extends:istio.mcp.v1alpha1.IncrementalMeshConfigResponse)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <pre>
   * The version of the response data (used for debugging).
   * </pre>
   *
   * <code>string system_version_info = 1;</code>
   * @return The systemVersionInfo.
   */
  java.lang.String getSystemVersionInfo();
  /**
   * <pre>
   * The version of the response data (used for debugging).
   * </pre>
   *
   * <code>string system_version_info = 1;</code>
   * @return The bytes for systemVersionInfo.
   */
  com.google.protobuf.ByteString
      getSystemVersionInfoBytes();

  /**
   * <pre>
   * The response resources wrapped in the common MCP *Resource*
   * message. These are typed resources that match the type url in the
   * IncrementalMeshConfigRequest.
   * </pre>
   *
   * <code>repeated .istio.mcp.v1alpha1.Resource resources = 2;</code>
   */
  java.util.List<com.alibaba.nacos.istio.model.mcp.Resource> 
      getResourcesList();
  /**
   * <pre>
   * The response resources wrapped in the common MCP *Resource*
   * message. These are typed resources that match the type url in the
   * IncrementalMeshConfigRequest.
   * </pre>
   *
   * <code>repeated .istio.mcp.v1alpha1.Resource resources = 2;</code>
   */
  com.alibaba.nacos.istio.model.mcp.Resource getResources(int index);
  /**
   * <pre>
   * The response resources wrapped in the common MCP *Resource*
   * message. These are typed resources that match the type url in the
   * IncrementalMeshConfigRequest.
   * </pre>
   *
   * <code>repeated .istio.mcp.v1alpha1.Resource resources = 2;</code>
   */
  int getResourcesCount();
  /**
   * <pre>
   * The response resources wrapped in the common MCP *Resource*
   * message. These are typed resources that match the type url in the
   * IncrementalMeshConfigRequest.
   * </pre>
   *
   * <code>repeated .istio.mcp.v1alpha1.Resource resources = 2;</code>
   */
  java.util.List<? extends com.alibaba.nacos.istio.model.mcp.ResourceOrBuilder> 
      getResourcesOrBuilderList();
  /**
   * <pre>
   * The response resources wrapped in the common MCP *Resource*
   * message. These are typed resources that match the type url in the
   * IncrementalMeshConfigRequest.
   * </pre>
   *
   * <code>repeated .istio.mcp.v1alpha1.Resource resources = 2;</code>
   */
  com.alibaba.nacos.istio.model.mcp.ResourceOrBuilder getResourcesOrBuilder(
      int index);

  /**
   * <pre>
   * Resources names of resources that have be deleted and to be
   * removed from the MCP Client.  Removed resources for missing
   * resources can be ignored.
   * </pre>
   *
   * <code>repeated string removed_resources = 3;</code>
   * @return A list containing the removedResources.
   */
  java.util.List<java.lang.String>
      getRemovedResourcesList();
  /**
   * <pre>
   * Resources names of resources that have be deleted and to be
   * removed from the MCP Client.  Removed resources for missing
   * resources can be ignored.
   * </pre>
   *
   * <code>repeated string removed_resources = 3;</code>
   * @return The count of removedResources.
   */
  int getRemovedResourcesCount();
  /**
   * <pre>
   * Resources names of resources that have be deleted and to be
   * removed from the MCP Client.  Removed resources for missing
   * resources can be ignored.
   * </pre>
   *
   * <code>repeated string removed_resources = 3;</code>
   * @param index The index of the element to return.
   * @return The removedResources at the given index.
   */
  java.lang.String getRemovedResources(int index);
  /**
   * <pre>
   * Resources names of resources that have be deleted and to be
   * removed from the MCP Client.  Removed resources for missing
   * resources can be ignored.
   * </pre>
   *
   * <code>repeated string removed_resources = 3;</code>
   * @param index The index of the value to return.
   * @return The bytes of the removedResources at the given index.
   */
  com.google.protobuf.ByteString
      getRemovedResourcesBytes(int index);

  /**
   * <pre>
   * The nonce provides a way for IncrementalMeshConfigRequests to
   * uniquely reference an IncrementalMeshConfigResponse. The nonce is
   * required.
   * </pre>
   *
   * <code>string nonce = 4;</code>
   * @return The nonce.
   */
  java.lang.String getNonce();
  /**
   * <pre>
   * The nonce provides a way for IncrementalMeshConfigRequests to
   * uniquely reference an IncrementalMeshConfigResponse. The nonce is
   * required.
   * </pre>
   *
   * <code>string nonce = 4;</code>
   * @return The bytes for nonce.
   */
  com.google.protobuf.ByteString
      getNonceBytes();
}
