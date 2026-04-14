/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.zreports_import;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 *
 * @author agian
 */
class Zreport_record {
    
    String year_month;
     LocalDate date1;                           // date
     String id_plaza;                           // plaza id
     String id_lane;                            // lane id
     BigDecimal gross = new BigDecimal(0);      // total lane gross
     int count1_1 = 0;                          // class 1 transactions with revenue
     BigDecimal net1_1 = new BigDecimal(0);     // class 1 net revenue
     int count2_1 = 0;                          // class 2 transactions with revenue
     BigDecimal net2_1 = new BigDecimal(0);     // class 2 net revenue
     int count3_1 = 0;                          // class 3 transactions with revenue
     BigDecimal net3_1 = new BigDecimal(0);     // class 3 net revenue
     int count4_1 = 0;                          // class 4 transactions with revenue
     BigDecimal net4_1 = new BigDecimal(0);     // class 4 net revenue
     BigDecimal total_net = new BigDecimal(0);  // net1_1+net2_1+net3_1+net4_1
     BigDecimal total_vat = new BigDecimal(0);  // (net1_1-vat)+(net2_1-vat)+(net3_1-vat)+(net4_1-vat)
     int count1_2 = 0;                          // class 1 transactions without revenue
     int count2_2 = 0;                          // class 2 transactions without revenue
     int count3_2 = 0;                          // class 3 transactions without revenue
     int count4_2 = 0;                          // class 4 transactions without revenue
     String id_network;                         // network id
     BigDecimal vat_rate;                       // vat rate corresponding to the date
/** the following properties are only used for pdf file creation
 *  they are not inserted into the database or the export file
 */
     String description_full_gr;             // plaza full name in greek

    
    // transactions with revenue: id_mop 1,7,12 cash, iou, bank card
    // transactions without revenue: id_mop 8,14,11 eme, exmpt, free passage
    
}
