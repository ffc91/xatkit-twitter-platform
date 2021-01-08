package com.xatkit.plugins.twitter.platform.action;

import static fr.inria.atlanmod.commons.Preconditions.checkArgument;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.List;

import com.github.seratch.jslack.api.model.Attachment;
import com.xatkit.core.platform.action.RuntimeAction;
import com.xatkit.execution.StateContext;
import com.xatkit.plugins.twitter.platform.TwitterPlatform;

import twitter4j.DirectMessage;
import twitter4j.DirectMessageList;
import twitter4j.Twitter;
import twitter4j.TwitterException;

/**
 * Shows the latest incoming direct menssages. Right now it can only return the first page of the results obtained
 * <p>
 * This class relies on the {@link TwitterPlatform}'s {@link twitter4j.Twitter} to integrate with twitter.
 */
public class ReceiveDM extends RuntimeAction<TwitterPlatform> {

    /**
     * The number of messages to retrieve per page, up to a maximum of 50. Defaults to 20.
     */
    private Integer messagesPerPage;

    /**
     * Shows the latest incoming direct menssages {@link ReceiveDM} with the provided {@code platform}, {@code context}.
     *
     * @param platform the {@link TwitterPlatform} containing this action
     * @param context  the {@link StateContext} associated to this action
     */
    public ReceiveDM(@NonNull TwitterPlatform platform, @NonNull StateContext context) {
        super(platform, context);
        this.messagesPerPage = 20;
    }

    /**
     * Shows the latest incoming direct menssages {@link ReceiveDM} with the provided {@code platform}, {@code context}.
     *
     * @param platform        the {@link TwitterPlatform} containing this action
     * @param context         the {@link StateContext} associated to this action
     * @param messagesPerPage the number of messages to retrieve per page
     */
    public ReceiveDM(@NonNull TwitterPlatform platform, @NonNull StateContext context,
            @NonNull Integer messagesPerPage) {
        super(platform, context);
        checkArgument((messagesPerPage > 0) && (messagesPerPage <= 50), "Cannot construct a %s "
                + "action with the provided messagesPerPage %s, expected a non-null, greater than 0 and less than or equal to 50 integer",
                this.getClass().getSimpleName(), messagesPerPage);
        this.messagesPerPage = messagesPerPage;
    }

    /**
     * Retrieves the latest incoming direct messages. Right now only the first page i retrieved.
     * 
     * @return 0 if there are no messages, or a list of attachments with the DMs formated for Slack
     * 
     *         TODO make formating flexibe create a formatter that is a parameter.
     */
    @Override
    protected Object compute() {
        String result = "0";
        Twitter twitterService = this.runtimePlatform.getTwitterService();
        List<Attachment> attachments = new ArrayList<>();

        try {
            DirectMessageList DMList = twitterService.getDirectMessages(messagesPerPage);
            if (!DMList.isEmpty()) {
                for (DirectMessage DM : DMList) {
                    if (!twitterService.getScreenName()
                            .equals(twitterService.showUser(DM.getSenderId()).getScreenName())) {

                        Attachment.AttachmentBuilder attachmentBuilder = Attachment.builder();
                        String authorName = twitterService.showUser(DM.getSenderId()).getName() + " @"
                                + twitterService.showUser(DM.getSenderId()).getScreenName();
                        String text = DM.getText();
                        attachmentBuilder.authorName(authorName);
                        attachmentBuilder.text(text);
                        attachmentBuilder.color("#1da1f2");
                        attachmentBuilder.ts(String.valueOf(DM.getCreatedAt().getTime() / 1000));
                        attachments.add(attachmentBuilder.build());
                    }
                }
            }

            if (attachments.size() > 0) {
                return attachments;
            }
        } catch (TwitterException e) {
            result = "1";
            e.printStackTrace();
        }
        return result;
    }
}
