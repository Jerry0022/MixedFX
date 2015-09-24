package de.mixedfx.network.messages;

import java.util.ArrayList;

import javax.inject.Inject;

import de.mixedfx.network.ConnectivityManager;
import de.mixedfx.network.user.User;

public abstract class IdentifiedMessage extends Message {
    private final Object fromUserID;
    private ArrayList<Object> toUserIDs;

    @Inject
    private ConnectivityManager<?> cm;

    public IdentifiedMessage() {
	this.fromUserID = this.cm.getMyUser().getIdentifier();
	this.toUserIDs = new ArrayList<>();
    }

    public Object getFromUserID() {
	return this.fromUserID;
    }

    public ArrayList<Object> getToUserIDs() {
	return this.toUserIDs;
    }

    /**
     * @param toUserIDs
     *            May not be null. If empty it is a broadcast otherwise a multi-
     *            or unicast.
     */
    public void setReceivers(final ArrayList<Object> toUserIDs) {
	if (toUserIDs == null) {
	    throw new IllegalArgumentException("Parameter toUserIDs may not be null!");
	}

	this.toUserIDs = toUserIDs;
    }

    /**
     * Only the identifier of the users will be used to map the message.
     *
     * @param toUsers
     *            If empty or null it is a broadcast otherwise a multi- or
     *            unicast.
     */
    public void setReceivers(final User... toUsers) {
	for (final User user : toUsers) {
	    this.toUserIDs.add(user.getIdentifier());
	}
    }
}
