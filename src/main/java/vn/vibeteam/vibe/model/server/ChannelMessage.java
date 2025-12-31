package vn.vibeteam.vibe.model.server;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import vn.vibeteam.vibe.model.authorization.User;
import vn.vibeteam.vibe.model.common.BaseEntity;

import java.util.List;
//import com.vladmihalcea.hibernate.type.json.JsonType;
//import org.hibernate.annotations.Type;

@Entity
@Table(name = "channel_messages")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class ChannelMessage extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "channel_id", nullable = false)
    private Long channelId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "channel_id", insertable = false, updatable = false)
    private Channel channel;

    @Column(name = "author_id", nullable = false)
    private Long authorId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", insertable = false, updatable = false)
    private User author;

    @Column(columnDefinition = "TEXT")
    private String content;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "attachments", columnDefinition = "jsonb")
    private List<MessageAttachment> attachmentMetadata;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "meta_data", columnDefinition = "jsonb")
    private MessageMetadata metadata;

    @Column(name = "is_pinned")
    private Boolean isPinned;

    @Column(name = "is_edited")
    private Boolean isEdited;
}

