/*
 * $Id$
 *
 * Copyright (C) XX, 2006
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package fr.inrialpes.exmo.align.service;

import java.lang.ClassNotFoundException;
import java.lang.IllegalAccessException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;


public class DBServiceImpl implements DBService{
    int id = 0;
    Connection conn = null;
    static String IPAddress = "localhost";
    static String port = "3306";
    static String user = "adminAServ";
    static String database = "AServDB";
    String driverPrefix = "jdbc:mysql";
    Statement st = null;
    Cache cache = null;
	
    public DBServiceImpl() throws ClassNotFoundException, InstantiationException, IllegalAccessException {
	Class.forName("com.mysql.jdbc.Driver").newInstance();
    }

    public DBServiceImpl( String driver, String prefix ) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
	Class.forName(driver).newInstance();
	driverPrefix = prefix;
    }

    public void init() {
    }
	 	
    public void connect( String password ) throws SQLException {
	connect( IPAddress, port, user, password );
    }
    
    public void connect( String user, String password ) throws SQLException {
	connect( IPAddress, port, user, password );
    }
    
    public void connect( String port, String user, String password ) throws SQLException {
	connect( IPAddress, port, user, password );
    }
    
    public void connect(String IPAddress, String port, String user, String password ) throws SQLException {
	connect( IPAddress, port, user, password, database );
	}

    public void connect(String IPAddress, String port, String user, String password, String database ) throws SQLException {
	conn = DriverManager.getConnection(driverPrefix+"://"+IPAddress+":"+port+"/"+database, user, password);
	st = (Statement) conn.createStatement();
	}

    public Connection getConnection() {
	return conn;
    }
	
    // JE: I think that there is no interest now
/*  public synchronized long nextID(){
	long id = 0;
	try {
	    st.executeUpdate("insert into id_seq (aa) values ('a')");
	    rs = (ResultSet) st.executeQuery("select max(id) from id_seq");
	    //System.out.println(rs.toString());
	    while(rs.next()) {
		id = rs.getInt(1);
	    }
	    //id = rs.getBigDecimal(1).longValue();
	} catch(Exception ex){
	    System.out.println(ex.toString());
	    return -1;
	}
	return id;
    } */
	
    public void close() {
	try {
	    conn.close();
	    st.close();
	} catch (Exception ex) {
	    ex.printStackTrace();
	}
    }
    
}
