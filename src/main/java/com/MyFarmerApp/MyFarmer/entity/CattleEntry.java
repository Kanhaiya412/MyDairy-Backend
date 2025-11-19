package com.MyFarmerApp.MyFarmer.entity;

import com.MyFarmerApp.MyFarmer.enums.CattleCategory;
import com.MyFarmerApp.MyFarmer.enums.CattleBreed;
import com.MyFarmerApp.MyFarmer.enums.CattleStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(
        name = "DIV_CATTLEENTRY",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"U_CATTLEID", "user_id"}) // ✅ Unique per user
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

    /**
     * 🔗 Relationship: Many cattle belong to one user (Farmer)
     * LAZY fetch = load only when accessed
     * Prevents loading full User object for every cattle
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    @JsonIgnore // 👈 prevents circular serialization (user → cattle → user)
    private User user;

    // 🆔 Cattle identifier (unique per user)
    @Column(name = "U_CATTLEID", nullable = false)
    private String cattleId;

    // 🐄 Category (COW / BUFFALO)
    @Enumerated(EnumType.STRING)
    @Column(name = "U_CATTLECATEGORY", nullable = false)
    private CattleCategory cattleCategory;

    // 🧬 Breed
    @Enumerated(EnumType.STRING)
    @Column(name = "U_CATTLEBREED", nullable = false)
    private CattleBreed cattleBreed;

    // 📅 Purchase date
    @Column(name = "U_CATTLEPURCHASEDATE", nullable = false)
    private LocalDate cattlePurchaseDate;

    // 🗓 Day of purchase (auto-filled)
    @Column(name = "U_CATTLEDAY", nullable = false)
    private String cattleDay;

    // 🏪 Purchase source
    @Column(name = "U_CATTLEPURCHASEFROM", nullable = false)
    private String cattlePurchaseFrom;

    // 🏷️ Name of the cattle
    @Column(name = "U_CATTLENAME", nullable = false)
    private String cattlename;

    // 💰 Purchase price
    @Column(name = "U_CATTLEPURCHASEPRICE", nullable = false)
    private Double cattlePurchasePrice;

    // 🧾 Sold info (optional)
    @Column(name = "U_CATTLESOLDDATE")
    private LocalDate cattleSoldDate;

    @Column(name = "U_CATTLESOLDTO")
    private String cattleSoldTo;

    @Column(name = "U_CATTLESOLDPRICE")
    private Double cattleSoldPrice;

    // 🧮 Total cattle owned
    @Column(name = "U_TOTALCATTLE", nullable = false)
    private Integer totalCattle;

    // ⚙️ Active/Sold/Archived status
    @Enumerated(EnumType.STRING)
    @Column(name = "U_STATUS")
    private CattleStatus status = CattleStatus.ACTIVE;

    // 🧩 Convenience constructor
    public CattleEntry(User user, String cattleId, CattleCategory category,
                       CattleBreed breed, LocalDate purchaseDate, String cattleDay,
                       String purchaseFrom, String cattlename, Double purchasePrice,
                       Integer totalCattle) {
        this.user = user;
        this.cattleId = cattleId;
        this.cattleCategory = category;
        this.cattleBreed = breed;
        this.cattlePurchaseDate = purchaseDate;
        this.cattleDay = cattleDay;
        this.cattlePurchaseFrom = purchaseFrom;
        this.cattlename = cattlename;
        this.cattlePurchasePrice = purchasePrice;
        this.totalCattle = totalCattle;
        this.status = CattleStatus.ACTIVE;
    }
}
