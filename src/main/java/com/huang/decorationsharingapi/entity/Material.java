package com.huang.decorationsharingapi.entity;

import com.vladmihalcea.hibernate.type.json.JsonStringType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "materials")
@TypeDef(name = "json", typeClass = JsonStringType.class)
/**public class Material {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "image_url", nullable = false)
    private String imageUrl;

    @Column(name = "thumb_url")
    private String thumbUrl;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column(columnDefinition = "INT DEFAULT 0")
    private Integer views = 0;

    @Column(columnDefinition = "INT DEFAULT 0")
    private Integer favorites = 0;

    @Type(type = "json")
    @Column(columnDefinition = "json")
    private List<String> tags = new ArrayList<>();

    private String license = "own";

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "ENUM('PENDING', 'APPROVED', 'REJECTED')")
    private Status status = Status.PENDING;

    @Column(name = "reject_reason", columnDefinition = "TEXT")
    private String rejectReason;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum Status {
        PENDING, APPROVED, REJECTED
    }
}
*/
public class Material {

    @Id // JPA: 标记为主键
    @GeneratedValue(strategy = GenerationType.IDENTITY) // JPA: 主键由数据库自增生成
    private Long id; // 素材的唯一标识符

    @Column(nullable = false, length = 100) // JPA: 定义列属性
    // nullable = false: 标题不能为空
    // length = 100: 标题最大长度为 100
    private String title; // 素材标题

    @Column(columnDefinition = "TEXT") // JPA: 定义列属性
    // columnDefinition = "TEXT": 数据库列类型为 TEXT，用于存储较长的描述信息
    private String description; // 素材详细描述

    @Column(name = "image_url", nullable = false) // JPA: 定义列属性
    // name = "image_url": 数据库列名为 "image_url"
    // nullable = false: 主图片 URL 不能为空
    private String imageUrl; // 素材主图片 URL

    @Column(name = "thumb_url") // JPA: 定义列属性
    // name = "thumb_url": 数据库列名为 "thumb_url"
    private String thumbUrl; // 素材缩略图 URL (可选)

    @ManyToOne(fetch = FetchType.LAZY) // JPA: 多对一关系，一个素材属于一个分类
    // fetch = FetchType.LAZY: 懒加载关联的 Category 对象
    @JoinColumn(name = "category_id") // JPA: 定义外键列 "category_id"，参照 "categories" 表的主键
    // nullable 默认为 true，表示素材可以没有分类（如果业务允许）
    private Category category; // 素材所属的分类

    @ManyToOne(fetch = FetchType.LAZY) // JPA: 多对一关系，一个素材由一个用户上传
    // fetch = FetchType.LAZY: 懒加载关联的 User 对象
    @JoinColumn(name = "user_id") // JPA: 定义外键列 "user_id"，参照 "users" 表的主键
    // nullable 默认为 true，但通常业务上素材应有关联用户，可能应设为 nullable = false
    private User user; // 上传该素材的用户

    @Column(columnDefinition = "INT DEFAULT 0") // JPA: 定义列属性
    // columnDefinition = "INT DEFAULT 0": 数据库列类型为 INT，并设置默认值为 0
    private Integer views = 0; // 浏览次数，Java 层面也初始化为 0

    @Column(columnDefinition = "INT DEFAULT 0") // JPA: 定义列属性
    // columnDefinition = "INT DEFAULT 0": 数据库列类型为 INT，并设置默认值为 0
    private Integer favorites = 0; // 收藏次数，Java 层面也初始化为 0
    // 这个字段通常是冗余字段，通过统计 Favorite 表的记录数得到。如果作为冗余字段，需要机制来更新它。

    @Type(type = "json") // Hibernate: 指定使用上面 @TypeDef 定义的名为 "json" 的自定义类型来处理此字段
    @Column(columnDefinition = "json") // JPA: 明确指定数据库列类型为 json (需要数据库支持 JSON 类型，如 PostgreSQL, MySQL 5.7+)
    private List<String> tags = new ArrayList<>(); // 素材标签列表，存储为 JSON 数组或 JSON 字符串。初始化为空列表。

    private String license = "own"; // 许可证信息，默认为 "own" (自有版权)。JPA 默认列名为 "license"。

    @Enumerated(EnumType.STRING) // JPA: 将枚举类型映射为字符串存储
    // EnumType.STRING: 在数据库中存储枚举的名称 (PENDING, APPROVED, REJECTED)
    // EnumType.ORDINAL (默认): 在数据库中存储枚举的序数 (0, 1, 2)，不推荐，因为修改枚举顺序会导致数据错乱
    @Column(columnDefinition = "ENUM('PENDING', 'APPROVED', 'REJECTED')") // JPA: 尝试定义数据库列类型为 ENUM (MySQL 特定语法)
    // 这个 columnDefinition 是 MySQL 特有的，如果使用其他数据库，可能需要调整或移除。
    // 如果移除，Hibernate 会根据 @Enumerated(EnumType.STRING) 将其映射为 VARCHAR。
    private Status status = Status.PENDING; // 素材状态，默认为 PENDING (待审核)

    @Column(name = "reject_reason", columnDefinition = "TEXT") // JPA: 定义列属性
    // name = "reject_reason": 数据库列名为 "reject_reason"
    // columnDefinition = "TEXT": 用于存储可能较长的拒绝理由
    private String rejectReason; // 如果素材被拒绝，记录拒绝理由

    @CreationTimestamp // Hibernate: 自动在实体创建时设置当前时间戳
    @Column(name = "created_at", updatable = false) // JPA: 定义列属性
    private LocalDateTime createdAt; // 素材创建时间

    @UpdateTimestamp // Hibernate: 自动在实体更新时设置当前时间戳
    @Column(name = "updated_at") // JPA: 定义列属性
    private LocalDateTime updatedAt; // 素材最后更新时间

    // 在实体类内部定义枚举 Status
    public enum Status {
        PENDING, APPROVED, REJECTED
    }
}