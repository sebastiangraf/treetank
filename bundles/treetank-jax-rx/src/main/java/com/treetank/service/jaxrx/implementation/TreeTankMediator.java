package com.treetank.service.jaxrx.implementation;

import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.core.StreamingOutput;

import org.jaxrx.JaxRx;
import org.jaxrx.core.JaxRxException;
import org.jaxrx.core.QueryParameter;
import org.jaxrx.core.ResourcePath;

import com.treetank.exception.TreetankException;
import com.treetank.service.jaxrx.enums.EIdAccessType;
import com.treetank.service.jaxrx.util.WorkerHelper;

/**
 * This class works as mediator between the JAX-RX REST interface layer and the
 * responsible Treetank implementation class. It checks the requested resource
 * path and forwards it then to the responsible one.
 * 
 * @author Lukas Lewandowski, University of Konstanz
 * 
 */
public final class TreeTankMediator implements JaxRx {

    /**
     * The instance of the database.
     */
    private final transient DatabaseRepresentation database = new DatabaseRepresentation();

    /**
     * The instance of access to a node id in a database.
     */
    private final transient NodeIdRepresentation nodeIdResource = new NodeIdRepresentation();

    /**
     * Not allowed message string.
     */
    private static final transient String NOTALLOWEDSTRING = "Method not allowed on this resource request";

    @Override
    public void add(final InputStream input, final ResourcePath path) throws JaxRxException {
        final int depth = path.getDepth();
        if (depth == 1) {
            database.add(input, path.getResourcePath());
        } else if (depth == 2) {
            nodeIdResource.addSubResource(path.getResource(0), Long.valueOf(path.getResource(1)), input,
                EIdAccessType.FIRSTCHILD);
        } else if (depth == 3) {
            final EIdAccessType accessType =
                WorkerHelper.getInstance().validateAccessType(path.getResource(2));
            nodeIdResource.addSubResource(path.getResource(0), Long.valueOf(path.getResource(1)), input,
                accessType);
        }
    }

    @Override
    public StreamingOutput command(final String command, final ResourcePath path) throws JaxRxException {

        // Here we have to discuss.... because on command in get AND post
        // request ... enforcement of REST concept

        if (command.equalsIgnoreCase("revert") && path.getDepth() == 1) {
            final String revision = path.getValue(QueryParameter.REVISION);
            if (revision != null) {
                try {
                    database.revertToRevision(path.getResourcePath(), Long.valueOf(revision));
                    return null;
                } catch (final NumberFormatException exce) {
                    throw new JaxRxException(400, "False value for REVISION paramter: " + exce.getMessage());
                } catch (final TreetankException exce) {
                    throw new JaxRxException(exce);
                }
            }
        }

        throw new JaxRxException(
            403,
            "Currently only 'revert' is accepted as COMMAND query parameter in a POST request. In GET requests we do not support COMMAND query parameters");
    }

    @Override
    public void update(final InputStream input, final ResourcePath path) throws JaxRxException {
        final int depth = path.getDepth();
        if (depth == 1) {
            database.createResource(input, path.getResourcePath());
        } else if (depth == 2) {
            nodeIdResource.modifyResource(path.getResource(0), Long.valueOf(path.getResource(1)), input);
        } else {
            throw new JaxRxException(405, NOTALLOWEDSTRING);
        }

    }

    @Override
    public void delete(final ResourcePath path) throws JaxRxException {
        final int depth = path.getDepth();
        switch (depth) {
        case 1:
            database.deleteResource(path.getResourcePath());
            break;
        case 2:
            nodeIdResource.deleteResource(path.getResource(0), Long.valueOf(path.getResource(1)));
            break;
        default:
            throw new JaxRxException(405, NOTALLOWEDSTRING);
        }
    }

    @Override
    public StreamingOutput get(final ResourcePath path) throws JaxRxException {
        final int depth = path.getDepth();
        StreamingOutput response;
        switch (depth) {
        case 0:
            response = database.getResourcesNames();
            break;
        case 1:
            response = database.getResource(path.getResourcePath(), path.getQueryParameter());
            break;
        case 2:
            response =
                nodeIdResource.getResource(path.getResource(0), Long.valueOf(path.getResource(1)), path
                    .getQueryParameter());
            break;
        case 3:
            final EIdAccessType accessType =
                WorkerHelper.getInstance().validateAccessType(path.getResource(2));
            if (accessType == null) {
                throw new JaxRxException(400, "The access type: " + path.getResource(2)
                + " is not supported.");

            } else {
                response =
                    nodeIdResource.getResourceByAT(path.getResource(0), Long.valueOf(path.getResource(1)),
                        path.getQueryParameter(), accessType);
            }
            break;
        default:
            throw new JaxRxException(405, NOTALLOWEDSTRING);
        }
        return response;
    }

    @Override
    public Set<QueryParameter> getParameters() {
        final Set<QueryParameter> availParams = new HashSet<QueryParameter>();
        availParams.add(QueryParameter.OUTPUT);
        availParams.add(QueryParameter.QUERY);
        availParams.add(QueryParameter.REVISION);
        availParams.add(QueryParameter.WRAP);
        availParams.add(QueryParameter.COMMAND);
        return availParams;
    }

    @Override
    public StreamingOutput query(final String query, final ResourcePath path) throws JaxRxException {
        StreamingOutput response;
        final int depth = path.getDepth();
        switch (depth) {
        case 1:
            response =
                database.performQueryOnResource(path.getResourcePath(), query, path.getQueryParameter());
            break;
        case 2:
            response =
                nodeIdResource.performQueryOnResource(path.getResource(0), Long.valueOf(path.getResource(1)),
                    query, path.getQueryParameter());
            break;
        default:
            throw new JaxRxException(405, NOTALLOWEDSTRING);
        }
        return response;
    }

    @Override
    public StreamingOutput run(final String file, final ResourcePath path) throws JaxRxException {
        throw new JaxRxException(403,
            "Currently no applicable RUN query parameter in a GET request within TreeTank.");
    }

}
