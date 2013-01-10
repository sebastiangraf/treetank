package org.treetank.filelistener.file.node;

import org.treetank.api.INode;
import org.treetank.api.INodeFactory;
import org.treetank.filelistener.exceptions.WrongFilenodeDataLengthException;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;

/**
 * 
 * @author Andreas Rain
 *
 */
public class FileNodeFactory implements INodeFactory{
	  /**
	   * {@inheritDoc}
	   */
	  @Override
	  public INode deserializeNode(byte[] pData) {
	    ByteArrayDataInput input = ByteStreams.newDataInput(pData);
	    long nodeKey = input.readLong();
	    long nextNodeKey = input.readLong();
	    boolean header = input.readBoolean();
	    boolean eof = input.readBoolean();
	    byte[] data = new byte[FileNode.FILENODESIZE];
	    input.readFully(data);
	    
	    FileNode node = null;
		try {
			node = new FileNode(nodeKey, data);
		    node.setNextNodeKey(nextNodeKey);
		    node.setHeader(header);
		    node.setEof(eof);
		} catch (WrongFilenodeDataLengthException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    return node;
	  }
}
