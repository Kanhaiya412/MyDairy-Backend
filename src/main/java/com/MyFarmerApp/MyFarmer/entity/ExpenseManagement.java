package com.MyFarmerApp.MyFarmer.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "DIV_EXPENSEMANAGEMENT")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class ExpenseManagement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 🧾 Unique Expense ID (for internal tracking and external references)
    @Column(name = "U_EXPENSEID", nullable = false, unique = true)
    private String expenseId;

    /**
     * 👤 Relationship: Many expenses belong to one user (farmer)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;

    /**
     * 🐄 Relationship: Expense may be related to a specific cattle
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "U_CATTLEID")
    @JsonIgnore
    private CattleEntry cattleEntry;

    // 🏷️ Item unique ID
    @Column(name = "U_ITEMID", nullable = false)
    private String itemId;

    // 📂 Category of the item (Feed, Medicine, Equipment, etc.)
    @Column(name = "U_ITEMCATEGORY", nullable = false)
    private String itemCategory;

    // 🧾 Name of the item
    @Column(name = "U_ITEMNAME", nullable = false)
    private String itemName;

    // ⚖️ Quality description (A+, Organic, etc.)
    @Column(name = "U_ITEMQUALITY")
    private String itemQuality;

    // 🔢 Quantity purchased
    @Column(name = "U_ITEMQUANTITY", nullable = false)
    private Double itemQuantity;

    // 💰 Price per item unit
    @Column(name = "U_ITEMPRICE", nullable = false)
    private Double itemPrice;

    // 🧮 Total cost of this item = quantity × price
    @Column(name = "U_TOTALCOST", nullable = false)
    private Double totalCost;

    // 🏪 Shop name or supplier
    @Column(name = "U_ITEMSHOPNAME")
    private String itemShopName;

    // 👤 Buyer (person who made the purchase)
    @Column(name = "U_ITEMBUYER")
    private String itemBuyer;

    // 👨‍💼 Shop owner’s name
    @Column(name = "U_SHOPOWNER")
    private String shopOwner;

    // 🗓️ Purchase date
    @Column(name = "U_PURCHASEDATE", nullable = false)
    private LocalDate purchaseDate;

    // 📅 Purchase day (auto-filled as string like "Monday")
    @Column(name = "U_PURCHASEDAY", nullable = false)
    private String purchaseDay;

    // 🏬 Physical or online shop
    @Column(name = "U_ITEMSHOP")
    private String itemShop;

    // 🧾 Total expense (useful for future aggregations or reports)
    @Column(name = "U_TOTALEXPENSE")
    private Double totalExpense;

    // 🗒️ Optional remarks or notes for additional info
    @Column(name = "U_REMARKS")
    private String remarks;

    // ⚙️ Status (ACTIVE / ARCHIVED / DELETED)
    @Column(name = "U_STATUS")
    private String status = "ACTIVE";
}
