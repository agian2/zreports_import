/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.zreports_import;

import com.agian.lib.db.operations;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author agian
 */
public class Zreport_get_vat_rate_periods {
    
    private final operations ope;
    private final ArrayList<Zreport_vat_rate_period> vat_rate_periods;
    
    /**
     * 
     * @param ope 
     */
    public Zreport_get_vat_rate_periods(operations ope){
        
        this.ope = ope;
        this.vat_rate_periods = new ArrayList<>();
        
    }
    
    
    
    /**
     * 
     * @return 
     */
    public boolean get_vat_periods(){
        
        Logger.getGlobal().log(Level.INFO, "I will now try to load the vat periods");
        
        String sql = "SELECT date_from, IFNULL(date_to, CURRENT_DATE) AS date_to, percentage FROM vat_periods";
        
    /* try to connect to the database */
        if(!ope.initialize_database_connection_mariaDb()){
            
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "I have failed to connect to the database, be seeing you...");
            return false;
            
        }
        
    /* try to initialize the statement */
        if(!ope.initialize_prepared_statement(sql)){

            Logger.getGlobal().log(Level.SEVERE, "I have failed to initialize the statement, be seeing you...");
            ope.close_connection();
            return false;
            
        }
        
    /* try to query the database */
        if(!ope.execute_p_statement_query(false)){
            
            Logger.getGlobal().log(Level.SEVERE, "I cannot query the monitoring database, be seeing you...");
            ope.close_connection();
            return false;            
            
        }
        
        ResultSet rs = ope.get_resultSet();
        
        if(rs == null){
            
            Logger.getGlobal().log(Level.SEVERE, "I could not find the vat periods, be seeing you...");
            ope.close_connection();
            return false; 
            
        }
        
        try{
        while(rs.next()){
            
            Zreport_vat_rate_period vp = new Zreport_vat_rate_period();
            
            vp.vat_start_date = rs.getDate("date_from").toLocalDate();
            vp.vat_end_date = rs.getDate("date_to").toLocalDate();
            vp.vat_rate = rs.getBigDecimal("percentage");
            
            vat_rate_periods.add(vp);

            
        }
        
        rs.close();
                
        } catch (SQLException ex) {

            Logger.getGlobal().log(Level.SEVERE, "database error: ");
            Logger.getGlobal().log(Level.SEVERE, ex.getMessage());
            Logger.getGlobal().log(Level.SEVERE, "I am unable to get the vat periods, be seeing you...");
            ope.close_connection();

            return false;

        }
        
    /** if you got this far then return true */
        ope.close_connection();
        Logger.getGlobal().log(Level.INFO, "I have finished loading the vat periods");
        return true;
        
    }

    /**
     * 
     * @return 
     */
    public ArrayList<Zreport_vat_rate_period> get_vat_rate_periods(){
        
        return vat_rate_periods;
        
    }
    
}
