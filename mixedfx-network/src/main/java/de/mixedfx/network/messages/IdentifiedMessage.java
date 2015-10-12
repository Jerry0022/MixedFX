package de.mixedfx.network.messages;

import de.mixedfx.network.user.User;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Arrays;

public abstract class IdentifiedMessage extends Message {
    private
    @Setter
    @Getter
    Object fromUserID;

    private
    @Getter
    ArrayList<Object> toUserIDs;

    public IdentifiedMessage() {
        this.toUserIDs = new ArrayList<>();
    }

    /**
     * @param toUserIDs
     *            May not be null. If empty it is a broadcast otherwise a multi-
     *            or unicast.
     */
    public void setReceivers(@NonNull ArrayList<Object> toUserIDs) {
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
        Arrays.asList(toUsers).forEach(user -> this.toUserIDs.add(user.getIdentifier()));
    }
}
