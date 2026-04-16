/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.zreports_import;

import com.agian.lib.db.operations;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

/**
 *
 * @author agian
 */
public class Zreport_get_last_zids {
    
    private final operations ope;
    private final HashMap<String, Integer> zreport_ids;
    private String tablename;
    
    /**
     * 
     * @param concession
     */
    public Zreport_get_last_zids(String concession){
        
        this.ope = new operations("ticketingdb.neaodos.local", "zreports", "zreports", "Ab1234!!Ab1234!!");
        zreport_ids = new HashMap<>();
        
        switch(concession){
                
            case "03" -> tablename = "zreports_ko_test";
            case "01" -> tablename = "zreports_no_test";
            default -> { System.out.println("invalid concession"); System.exit(2);}
                
        }
        
    }
    
    /**
     * 
     * @return 
     */
    public boolean retrieve_last_zids(){
        

            String sql = "SELECT id_network AS id_n, id_plaza AS id_p, id_lane AS id_l, max(id_zreport) AS id_z FROM " + tablename + " GROUP BY id_network, id_plaza, id_lane";
            
            if(!ope.initialize_database_connection_mariaDb()){
                
                System.out.println("I have failed to connect to the zreport database, be seeing you...");
                System.exit(2);

            }
            
            if(!ope.initialize_prepared_statement(sql)){
                
                System.out.println("I have failed to initialize the statement, be seeing you...");
                ope.close_connection();
                System.exit(2);
                
            }
            
            if(!ope.execute_p_statement_query(true)){
                
                System.out.println("I have failed to execute the query, be seeing you...");
                ope.close_connection();
                System.exit(2);
                
            }
            
        try {
            ResultSet rs = ope.get_resultSet();
            
            if(rs == null){
                
                System.out.println("query for the zreport ids returned null, be seeing you...");
                System.exit(2);

            }
            
//            if(!rs.isBeforeFirst()){
//                
//                System.out.println("query for the zreport ids returned empty resultset, be seeing you...");
//                rs.close();
//                System.exit(2);
//                
//            }
            
            while(rs.next()){
                
                zreport_ids.put(rs.getString("id_n") + ";" + rs.getString("id_p") + ";" + rs.getString("id_l"), rs.getInt("id_z"));
                
                
            }
            
            rs.close();
            return true;
        } catch (SQLException ex) {
            System.getLogger(Zreport_get_last_zids.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
            return false;
        }
        
    }
    
    /**
     * 
     * @return 
     */
    public HashMap<String, Integer> get_last_z_ids(){
        
        return zreport_ids;
        
    }
    
}
