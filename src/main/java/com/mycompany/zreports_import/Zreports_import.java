/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.mycompany.zreports_import;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.stream.Stream;
import com.agian.lib.db.operations;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 *
 * @author agian
 */
public class Zreports_import {
    
    private final ArrayList<Zreport_record> zreport_records;
    private final HashMap<String, String> id_plaza_ko;
    private final HashMap<String, String> id_plaza_no;
    private final HashMap<String, String> description_full_gr_ko;
    private final HashMap<String, String> description_full_gr_no;
    private ArrayList<Zreport_vat_rate_period> vat_rate_periods;
    private final operations ope;
   

    /**
     * 0, what to do, 'import' or 'id'
     * 2, filename
     * 1, concession, '01' or '03'
     * @param args 
     */
    public static void main(String[] args) {
        System.out.println("Hello World!");
        
        Zreports_import zi = new Zreports_import();
        
        switch(args[0]){
            
            case "import" -> zi.do_import(args[2], args[1]);
            case "id" -> zi.create_id_zreport(args[1]);
            default -> System.out.println("'import' or 'id'");
            
        }
        
    }

    /**
     * 
     */    
    Zreports_import(){
        
        zreport_records = new ArrayList<>();
        id_plaza_ko = new HashMap<>();
        id_plaza_no = new HashMap<>();
        description_full_gr_ko = new HashMap<>();
        description_full_gr_no = new HashMap<>();
        vat_rate_periods = new ArrayList<>(); 
        ope = new operations("monitoring.neaodos.local", "tcstrx", "mychecks", "Ab1234!!Ab1234!!");
        
    }
    
    private void create_id_zreport(String concession){
        
        Zreport_get_last_zids zglz = new Zreport_get_last_zids(concession);
        
        zglz.retrieve_last_zids();
        
        HashMap<String, Integer> zreport_ids = zglz.get_last_z_ids();
        System.out.println("zreport_ids: " + zreport_ids.size());
        
        System.out.println("done");
        
    }
    
    /**
     * 
     * @param filename
     * @param concession 
     */
    private void do_import(String filename, String concession){
        
        switch(concession){
            
            case "01" -> System.out.println("nea");
            case "03" -> System.out.println("kentriki");
            default -> {System.out.println("error: '01' or '03'");System.exit(2);}
            
        }

        System.out.println("filename: '" + filename + "'");
        
        create_hashmaps(concession, ope);
        Zreport_get_vat_rate_periods vp = new Zreport_get_vat_rate_periods(ope);
        vp.get_vat_periods();
        vat_rate_periods = vp.get_vat_rate_periods();
        
        Path path = Path.of(filename);
        Stream<String> lines;
        try {
            
            lines = Files.lines(path);
            lines.forEach(line -> parse_line(line, concession));
            lines.close();
            
        } catch (IOException ex) {
            
            System.getLogger(Zreports_import.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
            System.out.println(ex.getMessage());
            
        }
            

        printout_records();
        insert_into_database(concession);

        
    }
    
    /**
     * 
     * @param line 
     */
    private boolean parse_line(String line, String concession){
        
        if(line.startsWith("DATE1;ID_PLAZA;ID_LANE;GROSS;COUNT1_1;NET1_1;COUNT2_1")){
           
            return false;
            
        }
        
        System.out.println("-> " + line);
        
        String[] l = line.split(";");
        
        Zreport_record zr = new Zreport_record();
        
        String date1 = l[0].substring(0, 10);
    
    // year_month
        zr.year_month = date1.replace("-", "").substring(0, 6);
    // date 1
        zr.date1 = LocalDate.parse(date1);
    // id_network
        zr.id_network = concession;
    // id_plaza
        if(concession.equalsIgnoreCase("03")){
            
            if(id_plaza_ko.get(l[1].replace("\"", "")) != null){

                zr.id_plaza = id_plaza_ko.get(l[1].replace("\"", ""));

            }else{

                return false;

            }
            
        }else if(concession.equalsIgnoreCase("01")){

            if(id_plaza_no.get(l[1].replace("\"", "")) != null){

                zr.id_plaza = id_plaza_no.get(l[1].replace("\"", ""));

            }else{

                return false;

            }            
            
        }else{
            
            return false;
            
        }
    // id_lane
        zr.id_lane = l[2];
    // gross
        zr.gross = new BigDecimal(l[3]);
    // count1_1
        zr.count1_1 = Integer.parseInt(l[4]);
    // net1_1
        zr.net1_1 = new BigDecimal(l[5]);
    // count2_1
        zr.count2_1 = Integer.parseInt(l[6]);
    // net2_1
        zr.net2_1 = new BigDecimal(l[7]);
    // count3_1
        zr.count3_1 = Integer.parseInt(l[8]);
    // net3_1
        zr.net3_1 = new BigDecimal(l[9]);
    // count4_1
        zr.count4_1 = Integer.parseInt(l[10]);
    // net4_1
        zr.net4_1 = new BigDecimal(l[11]);
    // count1_2
        zr.count1_2 = Integer.parseInt(l[12]);
    // count2_2
        zr.count2_2 = Integer.parseInt(l[13]);
    // count3_2
        zr.count3_2 = Integer.parseInt(l[14]);
    // count4_2
        zr.count4_2 = Integer.parseInt(l[15]);
    // total_net
        zr.total_net = zr.net1_1.add(zr.net2_1).add(zr.net3_1).add(zr.net4_1);
    // total_vat
        zr.total_vat = zr.gross.subtract(zr.total_net);
    // vat_rate
        zr.vat_rate = determine_vat_rate(zr.date1);
    // description_full_gr
        if(concession.equalsIgnoreCase("03")){
        
            zr.description_full_gr = description_full_gr_ko.get(l[1].replace("\"", ""));
            
        }else if(concession.equalsIgnoreCase("01")){
            
            zr.description_full_gr = description_full_gr_no.get(l[1].replace("\"", ""));
            
        }else{
            
            return false;
            
        }
        
        zreport_records.add(zr);
        
        
        return true;
        
    }
    
    private void printout_records(){
        
        for(Zreport_record zr : zreport_records){
            
            System.out.print(zr.year_month + ";");
            System.out.print(zr.date1.toString() + ";");
            System.out.print(zr.id_network + ";");
            System.out.print(zr.id_plaza + ";");
            System.out.print(zr.id_lane + ";");
            System.out.print(zr.gross + ";");
            System.out.print(zr.count1_1 + ";");
            System.out.print(zr.net1_1 + ";");
            System.out.print(zr.count2_1 + ";");
            System.out.print(zr.net2_1 + ";");
            System.out.print(zr.count3_1 + ";");
            System.out.print(zr.net3_1 + ";");
            System.out.print(zr.count4_1 + ";");
            System.out.print(zr.net4_1 + ";");
            System.out.print(zr.count1_2 + ";");
            System.out.print(zr.count2_2 + ";");
            System.out.print(zr.count3_2 + ";");
            System.out.print(zr.count4_2 + ";");
            System.out.print(zr.total_net + ";");
            System.out.print(zr.total_vat + ";");
            System.out.print(zr.vat_rate + ";");
            System.out.print(zr.description_full_gr);
            
            System.out.println();
            
        }
        
    }
    
    /**
     * 
     * @param concession
     * @param ope 
     */
    private void create_hashmaps(String concession, operations ope){
        
        try {

            String sql = "SELECT plaza_id, bos_plaza_id, description_full_gr FROM plaza_servers WHERE plaza_network = ?";
            
            if(!ope.initialize_database_connection_mariaDb()){
                
                System.out.println("I have failed to connect to the zreport database, be seeing you...");
                System.exit(2);
                
            }
            
            if(!ope.initialize_prepared_statement(sql)){
                
                System.out.println("I have failed to initialize the statement, be seeing you...");
                ope.close_connection();
                System.exit(2);
                
            }
 
            ope.p_stmt_setString(1, concession);
            
            if(!ope.execute_p_statement_query(false)){
                
                System.out.println("I have failed to fetch records from the database, be seeing you...");
                ope.close_connection();
                System.exit(2);
                
            }
            
            ResultSet rs = ope.get_resultSet();
            
            while(rs.next()){            
            
                if(concession.equalsIgnoreCase("03")){
                
                    id_plaza_ko.put(rs.getString("bos_plaza_id"), rs.getString("plaza_id"));
                    description_full_gr_ko.put(rs.getString("bos_plaza_id"), rs.getString("description_full_gr"));
                    
                }else if(concession.equalsIgnoreCase("01")){
                    
                    id_plaza_no.put(rs.getString("bos_plaza_id"), rs.getString("plaza_id"));
                    description_full_gr_no.put(rs.getString("bos_plaza_id"), rs.getString("description_full_gr"));
                    
                }
                
            }
            
            rs.close();
            ope.close_connection();
            
        } catch (SQLException ex) {
            System.getLogger(Zreports_import.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
        }
        
    }
    
    /**
     * 
     * @param date
     * @return 
     */
    private BigDecimal determine_vat_rate(LocalDate date){
       
        for(Zreport_vat_rate_period vp : vat_rate_periods){
           
            if (isBetweenInclusive(date, vp.vat_start_date, vp.vat_end_date)) {
            
                return vp.vat_rate;
        
            }
           
       }
       
        return null;
        
    }
    
    /**
     * 
     * @param concession
     * @return 
     */
    private boolean insert_into_database(String concession){
     
        String sql;
        operations ope = new operations("ticketingdb.neaodos.local", "zreports", "zreports", "Ab1234!!Ab1234!!");
        
        if(!ope.initialize_database_connection_mariaDb()){

            System.out.println("I have failed to connect to the zreport database, be seeing you...");
            System.exit(2);

        }
        
        if(concession.equalsIgnoreCase("03")){
            
            sql = "DELETE FROM zreports_ko";
            
        }else{
            
            sql = "DELETE FROM zreports_no";
            
        }
        
        if(!ope.initialize_prepared_statement(sql)){

            System.out.println("I have failed to initialize the statement, be seeing you...");
            ope.close_connection();
            System.exit(2);

        }
        
        if(!ope.execute_p_statement_update(false)){
            
            System.out.println("I have failed to delete existing records, be seeing you...");
            ope.close_connection();
            System.exit(2);
            
        }

        
        if(!ope.initialize_database_connection_mariaDb()){

            System.out.println("I have failed to connect to the zreport database, be seeing you...");
            System.exit(2);

        }
        
        sql = "INSERT INTO ";
        
        if(concession.equalsIgnoreCase("03")){
            
            sql = sql + "zreports_ko ";
        
        }else{
            
            sql = sql + "zreports_no ";
            
        }
        
        sql = sql + "(yearmonth, date1, id_plaza, id_lane, gross, count1_1, net1_1, count2_1, net2_1, count3_1, net3_1, count4_1, net4_1, count1_2, count2_2, count3_2, count4_2, id_network, vat_rate, total_net, total_vat, description_full_gr, id_zreport) "
                + "VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        

        
        if(!ope.initialize_prepared_statement(sql)){

            System.out.println("I have failed to initialize the statement, be seeing you...");
            ope.close_connection();
            System.exit(2);

        }
        
        for(Zreport_record zr: zreport_records){
            
            ope.p_stmt_setString(1, zr.year_month);
            ope.p_stmt_setDate(2, java.sql.Date.valueOf(zr.date1));
            ope.p_stmt_setString(3, zr.id_plaza);
            ope.p_stmt_setString(4, zr.id_lane);
            ope.p_stmt_setBigDecimal(5, zr.gross);
            ope.p_stmt_setInt(6, zr.count1_1);
            ope.p_stmt_setBigDecimal(7, zr.net1_1);
            ope.p_stmt_setInt(8, zr.count2_1);
            ope.p_stmt_setBigDecimal(9, zr.net2_1);
            ope.p_stmt_setInt(10, zr.count3_1);
            ope.p_stmt_setBigDecimal(11, zr.net3_1);
            ope.p_stmt_setInt(12, zr.count4_1);
            ope.p_stmt_setBigDecimal(13, zr.net4_1);
            ope.p_stmt_setInt(14, zr.count1_2);
            ope.p_stmt_setInt(15, zr.count2_2);
            ope.p_stmt_setInt(16, zr.count3_2);
            ope.p_stmt_setInt(17, zr.count4_2);
            ope.p_stmt_setString(18, zr.id_network);
            ope.p_stmt_setBigDecimal(19, zr.vat_rate);
            ope.p_stmt_setBigDecimal(20, zr.total_net);
            ope.p_stmt_setBigDecimal(21, zr.total_vat);
            ope.p_stmt_setString(22, zr.description_full_gr);
            ope.p_stmt_setInt(23, 0);
            
            ope.p_stmt_add_batch();
            
        }
        
        ope.execute_p_statement_batch(true);
        
        return true;
        

    }
    
    /**
     * 
     * @param date
     * @param start
     * @param end
     * @return 
     */
    private boolean isBetweenInclusive(LocalDate date, LocalDate start, LocalDate end) {
        
        return !date.isBefore(start) && !date.isAfter(end);
    
    }
    
}
