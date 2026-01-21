package com.MyFarmerApp.MyFarmer.entity;

import com.MyFarmerApp.MyFarmer.enums.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(
        name = "DIV_CATTLEENTRY",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"U_CATTLEID", "user_id"})
        }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class CattleEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // User Relationship
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;

    // Unique cattle ID for this user
    @Column(name = "U_CATTLEID", nullable = false)
    private String cattleId;

    @Enumerated(EnumType.STRING)
    @Column(name = "U_CATTLECATEGORY", nullable = false)
    private CattleCategory cattleCategory;

    @Enumerated(EnumType.STRING)
    @Column(name = "U_CATTLEBREED", nullable = false)
    private CattleBreed cattleBreed;

    // 🧬 Gender (NEW)
    @Enumerated(EnumType.STRING)
    @Column(name = "U_GENDER")
    private CattleGender gender;

    // Purchase Details
    @Column(name = "U_CATTLEPURCHASEDATE", nullable = false)
    private LocalDate cattlePurchaseDate;

    @Column(name = "U_CATTLEDAY", nullable = false)
    private String cattleDay;

    @Column(name = "U_CATTLEPURCHASEFROM", nullable = false)
    private String cattlePurchaseFrom;

    @Column(name = "U_CATTLENAME", nullable = false)
    private String cattleName;

    @Column(name = "U_CATTLEPURCHASEPRICE", nullable = false)
    private Double cattlePurchasePrice;

    // Sold Details (Optional)
    @Column(name = "U_CATTLESOLDDATE")
    private LocalDate cattleSoldDate;

    @Column(name = "U_CATTLESOLDTO")
    private String cattleSoldTo;

    @Column(name = "U_CATTLESOLDPRICE")
    private Double cattleSoldPrice;

    // Total cattle count in lot
    @Column(name = "U_TOTALCATTLE", nullable = false)
    private Integer totalCattle;

    // Image URL (NEW)
    @Column(name = "U_IMAGEURL")
    private String imageUrl;

    // Status
    @Enumerated(EnumType.STRING)
    @Column(name = "U_STATUS")
    private CattleStatus status = CattleStatus.ACTIVE;

    // Timestamps
    @Column(name = "U_CREATEDAT")
    private LocalDate createdAt = LocalDate.now();

    @Column(name = "U_UPDATEDAT")
    private LocalDate updatedAt = LocalDate.now();
}
