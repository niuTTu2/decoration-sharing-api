package com.huang.decorationsharingapi.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;
//分类
@Data // Lombok: 自动生成 @ToString, @EqualsAndHashCode, @Getter (所有字段), @Setter (所有非final字段), @RequiredArgsConstructor
@Entity // JPA: 声明这是一个实体类，将映射到数据库表
@Builder // Lombok: 实现建造者模式，可以通过 Category.builder().name("...").build() 创建对象
@NoArgsConstructor // Lombok: 生成一个无参构造函数
@AllArgsConstructor // Lombok: 生成一个包含所有字段的构造函数
@Table(name = "categories") // JPA: 明确指定映射到的数据库表名为 "categories"
public class Category {

    @Id // JPA: 标记此字段为表的主键
    @GeneratedValue(strategy = GenerationType.IDENTITY) // JPA: 主键生成策略。IDENTITY 表示由数据库自动递增生成 (例如 MySQL的 AUTO_INCREMENT)
    private Long id; // 主键，类型为 Long

    @Column(nullable = false, unique = true, length = 50) // JPA: 定义列属性
    // nullable = false: 该列不允许为 NULL
    // unique = true: 该列的值必须唯一，数据库层面会建立唯一约束
    // length = 50: 字符串类型的最大长度为 50
    private String name; // 分类名称

    @Column(columnDefinition = "TEXT") // JPA: 定义列属性
    // columnDefinition = "TEXT": 明确指定数据库列类型为 TEXT，用于存储较长的文本描述
    // (注意: 不同数据库对TEXT类型的支持和具体名称可能略有差异，但JPA会尝试适配)
    private String description; // 分类描述

    @Column(name = "icon_url") // JPA: 定义列属性
    // name = "icon_url": 数据库中的列名将是 "icon_url"
    private String iconUrl; // 分类图标的 URL

    private String color; // 分类颜色 (例如 "#FFFFFF")。这里没有 @Column 注解，JPA 会默认使用字段名 "color"作为列名。

    private Integer sort = 0; // 分类排序字段，默认为 0。同样，JPA 会默认使用字段名 "sort"作为列名。
    // 初始化为0是一个好习惯，避免了 null 值。

    @CreationTimestamp // Hibernate: 自动在实体创建时设置此字段的值为当前时间戳
    @Column(name = "created_at", updatable = false) // JPA: 定义列属性
    // name = "created_at": 数据库中的列名将是 "created_at"
    // updatable = false: 一旦设置，此字段的值在后续更新操作中不会被改变
    private LocalDateTime createdAt; // 记录创建时间，使用 Java 8 的 LocalDateTime

    @UpdateTimestamp // Hibernate: 自动在实体更新时设置此字段的值为当前时间戳
    @Column(name = "updated_at") // JPA: 定义列属性
    // name = "updated_at": 数据库中的列名将是 "updated_at"
    private LocalDateTime updatedAt; // 记录最后更新时间
}