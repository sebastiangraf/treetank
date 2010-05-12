package com.treetank.service.jaxrx.enums;

/**
 * This {@link Enum} class offers the available set of HTTP method node id
 * resource access types.
 * 
 * @author Lukas Lewandowski, University of Konstanz
 * 
 */
public enum EIdAccessType {
	/**
	 * Access to the left sibling node of the resource identified by a node id.
	 */
	LEFTSIBLING,
	/**
	 * Access to the right sibling node of the resource identified by a node id.
	 */
	RIGHTSIBLING,
	/**
	 * Access to the first child node of the resource identified by a node id.
	 */
	FIRSTCHILD,
	/**
	 * Access to the last child node of the resource identified by a node id.
	 */
	LASTCHILD;
}
