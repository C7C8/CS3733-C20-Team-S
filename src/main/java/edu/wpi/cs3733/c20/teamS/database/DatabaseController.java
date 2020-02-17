package edu.wpi.cs3733.c20.teamS.database;

import edu.wpi.cs3733.c20.teamS.ThrowHelper;
import edu.wpi.cs3733.c20.teamS.serviceRequests.JanitorServiceRequest;
import edu.wpi.cs3733.c20.teamS.serviceRequests.RideServiceRequest;
import edu.wpi.cs3733.c20.teamS.serviceRequests.ServiceRequest;
import edu.wpi.cs3733.c20.teamS.serviceRequests.ServiceVisitor;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.sql.*;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

public class DatabaseController {
    private static Connection connection = null;

    /**
     * Ensures there is only one connection to the database;
     */
    static {
        createDatabase();
        dropExistingTables();

        /////////////////////CREATE TABLES/////////////////////////////
        try {
            Statement stm = connection.createStatement();

            createNodesTable(stm);
            createEdgeTable(stm);
            createEmployeeTable(stm);
            createServiceableTable(stm);
            createServicesTable(stm);
        }
        catch (SQLException e){
            System.out.print("Failed to create one of tables, aborting...");
            throw new RuntimeException();
        }
    }
    public DatabaseController() {}

    private static void createDatabase() {
        try {
            Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
        }catch(ClassNotFoundException ex){
            System.out.println(ex.getMessage());
            System.out.println("Failed to get embedded driver");
            throw new RuntimeException();
        }
        try{
            connection = DriverManager.getConnection("jdbc:derby:hospitalDB;create=true");
            Statement init = connection.createStatement();
        }catch(SQLException ex){
            System.out.println(ex.getMessage());
            System.out.println("Failed to connect to database");
            throw new RuntimeException();
        }
    }
    private static void dropExistingTables() {
        try{
            Statement stm = connection.createStatement();
            stm.execute("Drop table EDGES");
        }catch(SQLException e){
            System.out.println("EDGES WAS NOT DROPPED");
        }

        System.out.println("Dropped Table Edges");


        try{
            Statement stm = connection.createStatement();
            stm.execute("Drop table SERVICES");
        }catch(SQLException e){
            System.out.println("SERVICES WAS NOT DROPPED");
        }

        System.out.println("Dropped Table SERVICES");

        try{
            Statement stm = connection.createStatement();
            stm.execute("Drop table NODES");
        }catch(SQLException e){
            System.out.println("NODES WAS NOT DROPPED");
        }

        System.out.println("Dropped Table Nodes");


        try{
            Statement stm = connection.createStatement();
            stm.execute("Drop table SERVICEABLE");
        }catch(SQLException e){
            System.out.println("SERVICEABLE WAS NOT DROPPED");
        }

        System.out.println("Dropped Table SERVICEABLE");

        try{
            Statement stm = connection.createStatement();
            stm.execute("Drop table EMPLOYEES");
        }catch(SQLException e){
            System.out.println("EMPLOYEES WAS NOT DROPPED");
        }

        System.out.println("Dropped Table EMPLOYEES");
    }
    private static void createServicesTable(Statement stm) throws SQLException {
        stm.execute("CREATE TABLE SERVICES(" +
                         "serviceID INTEGER GENERATED BY DEFAULT AS IDENTITY constraint pKey_serviceID primary key," +
                         "serviceType varchar(4)," +
                         "status varchar(50)," +
                         "message varchar(2056)," +
                         "assignedEmployee INTEGER CONSTRAINT fKey_empAssigned references EMPLOYEES (employeeID)," +
                         "timeCreated DATE," +
                         "location varchar(1024) constraint fKey_nodeService references NODES (nodeid))");
        System.out.println("Created Table SERVICES");
    }
    private static void createServiceableTable(Statement stm) throws SQLException {
        stm.execute("CREATE TABLE SERVICEABLE(" +
                         "employeeID INTEGER CONSTRAINT fKey_service references EMPLOYEES(employeeID)," +
                         "serviceType varchar(4)," +
                         "constraint pKey_canDO primary key (employeeID, serviceType))");
        System.out.println("Created Table SERVICEABLE");
    }
    private static void createEmployeeTable(Statement stm) throws SQLException {
        stm.execute("CREATE TABLE EMPLOYEES(" +
                        "employeeID INTEGER GENERATED BY DEFAULT AS IDENTITY CONSTRAINT pKey_empID primary key," +
                        "userName varchar(50) constraint uKey_userName unique," +
                        "password varchar(25)," +
                        "accessLevel INTEGER," +
                        "firstName varchar(30)," +
                        "lastName varchar(30))");
        System.out.println("Created Table Employees");
    }
    private static void createEdgeTable(Statement stm) throws SQLException {
        stm.execute("CREATE TABLE EDGES(" +
                        "edgeID varchar(1024) constraint pKey_edgeID PRIMARY KEY," +
                        "startNode varchar(1024) constraint fKey_startNodeID references NODES (nodeID)," +
                        "endNode varchar(1024) constraint fkey_endNodeID references NODES (nodeID))");
        System.out.println("Created Table Edges");
    }
    private static void createNodesTable(Statement stm) throws SQLException {
        stm.execute("CREATE TABLE NODES(" +
                        "nodeID VARCHAR(1024)," +
                        "xcoord double," +
                        "ycoord double," +
                        "floor int," +
                        "building varchar(50)," +
                        "nodeType varchar(4)," +
                        "longName varchar(1024)," +
                        "shortName varchar(50)," +
                        "constraint pkey_nodeID Primary Key (nodeID))");
        System.out.println("Created Table Nodes");
    }

    //Tested
    public void addNode(NodeData nd){
        String insert = "INSERT INTO NODES VALUES(?,?,?,?,?,?,?,?)";
        try{
            PreparedStatement stm = connection.prepareStatement(insert);
            stm.setString(1,nd.getNodeID());
            stm.setDouble(2,nd.getxCoordinate());
            stm.setDouble(3,nd.getyCoordinate());
            stm.setInt(4, nd.getFloor());
            stm.setString(5,nd.getBuilding());
            stm.setString(6,nd.getNodeType());
            stm.setString(7,nd.getLongName());
            stm.setString(8,nd.getShortName());
            stm.execute();
        }
        catch (SQLException e) {
            System.out.println("Failed to insert into nodes");
            throw new RuntimeException();
        }
    }
    //Tested
    public void addSetOfNodes(Set<NodeData> set){
        for(NodeData nd : set){
            addNode(nd);
        }
    }
    //Tested
    public Set<NodeData> getAllNodes(){
        Statement stm = null;
        try{
            stm = connection.createStatement();
        }catch(java.sql.SQLException e){
            System.out.println(e.getMessage());
            throw new RuntimeException(e);
        }
        String allNodeString = "SELECT * FROM Nodes";
        ResultSet rset = null;
        Set<NodeData> nodeSet = new HashSet<>();
        try {
            rset = stm.executeQuery(allNodeString);
        }catch(java.sql.SQLException state){
            System.out.println(state.getMessage());
            state.printStackTrace();
            throw new RuntimeException(state);
        }
        nodeSet = parseNodeResultSet(rset);
        try{
            rset.close();
        }catch(java.sql.SQLException e){
            System.out.println(e.getMessage());
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        return nodeSet;
    }
    //Tested
    public Set<NodeData> parseNodeResultSet(ResultSet rset) {
        Set<NodeData> nodeSet = new HashSet<NodeData>();
        String nodeID;
        double x, y;
        int floor;
        String building;
        String nodeType;
        String longName;
        String shortName;
        try {
            while (rset.next()) {
                nodeID = rset.getString("nodeID");
                x = rset.getDouble("xcoord");
                y = rset.getDouble("ycoord");
                floor = rset.getInt("floor");
                building = rset.getString("building");
                nodeType = rset.getString("nodeType");
                longName = rset.getString("longName");
                shortName = rset.getString("shortName");
                nodeSet.add(new NodeData(nodeID, x, y, floor, building, nodeType, longName, shortName));
            }

        } catch (java.sql.SQLException rsetFailure) {
            System.out.println(rsetFailure.getMessage());
            rsetFailure.printStackTrace();
            throw new RuntimeException(rsetFailure);
        }

        return nodeSet;
    }
    //Tested
    public Set<EdgeData> getAllEdges(){
        Statement stm = null;
        try{
            stm = connection.createStatement();
        }catch(java.sql.SQLException e){
            System.out.println(e.getMessage());
        }
        String allNodeString = "SELECT * FROM Edges";
        ResultSet rset = null;
        try {
            rset = stm.executeQuery(allNodeString);
        }catch(java.sql.SQLException state){
            System.out.println(state.getMessage());
            state.printStackTrace();
        }
        Set<EdgeData> edgeSet = parseEdgeResultSet(rset);
        try{
            rset.close();
        }catch(java.sql.SQLException e){
            System.out.println(e.getMessage());
            e.printStackTrace();
            throw new RuntimeException();
        }
        return edgeSet;
    }
    //Tested
    public Set<EdgeData> parseEdgeResultSet(ResultSet rset){
        String edgeID;
        String startNodeID;
        String endNodeID;
        Set<EdgeData> edgeSet = new HashSet<>();
        try{
            while(rset.next()){
                edgeID = rset.getString("edgeID");
                startNodeID = rset.getString("startNode");
                endNodeID = rset.getString("endNode");
                edgeSet.add(new EdgeData(edgeID, startNodeID, endNodeID));
            }
        }catch(java.sql.SQLException rsetFailure){
            System.out.println(rsetFailure.getMessage());
            rsetFailure.printStackTrace();
            throw new RuntimeException();
        }
        return edgeSet;
    }
    //Tested
    public void addEdge(EdgeData edge) {
        String addEntryStr = "INSERT INTO EDGES VALUES (?, ?, ?)";
        try {
            PreparedStatement addStm = connection.prepareCall(addEntryStr);
            addStm.setString(1,edge.getEdgeID());
            addStm.setString(2,edge.getStartNode());
            addStm.setString(3,edge.getEndNode());
            addStm.execute();
            addStm.close();
        }catch(SQLException e){
            throw new RuntimeException();
        }
    }
    //Tested
    public NodeData getNode(String ID){
        String getNodeStr = "SELECT * FROM NODES WHERE NODEID = ?";
        ResultSet rset = null;
        try {
            PreparedStatement getStm = connection.prepareCall(getNodeStr);
            getStm.setString(1,ID);
            rset = getStm.executeQuery();
        }catch(SQLException e){
            System.out.println(e.getMessage());
            throw new RuntimeException();
        }
        Set<NodeData> nodeSet = parseNodeResultSet(rset);
        if(nodeSet.isEmpty()){
            System.out.println("Node Not Found");
            return null;
        }
        System.out.println("Got Node");
        NodeData returnNode = null;
        for(NodeData nd : nodeSet){
            returnNode = nd;

        }
        return returnNode;
    }
    //Tested
    public void importStartUpData() {

        String path = getClass().getResource("/data/allnodes.csv").toString();
        path = path.substring(6);
        System.out.println("Getting Nodes from: " + path);
        importData("NODES", path, true);


        String edgePath = getClass().getResource("/data/allEdges.csv").toString();
        edgePath = edgePath.substring(6);
        System.out.println("Getting Edges from: " + edgePath);
        importData("EDGES", edgePath, true);

        String empPath = getClass().getResource("/data/employees.csv").toString();
        empPath = empPath.substring(6);
        System.out.println("Getting Employees from: " + empPath);
        importData("EMPLOYEES", empPath, true);
    }
    //Tested
    public int importData(String toTable, String filePath, boolean withHeader){



        //File newFile = new File("resources/data/allnodes.csv");
        //System.out.println(newFile.getAbsolutePath());

        try {
            //Prepares statement with call
            CallableStatement importStatement;
            importStatement = connection.prepareCall("{call SYSCS_UTIL.SYSCS_IMPORT_TABLE_BULK(?,?,?,?,?,?,?,?)}");
            importStatement.setNull(1, Types.VARCHAR);
            importStatement.setString(2,toTable);
            importStatement.setString(3,filePath);
            importStatement.setNull(4,Types.VARCHAR);
            importStatement.setNull(5,Types.VARCHAR);
            importStatement.setNull(6,Types.VARCHAR);
            importStatement.setInt(7,0);
            if(withHeader){
                importStatement.setInt(8,1);
            }else{
                importStatement.setInt(8,0);
            }
            importStatement.execute();
            importStatement.close();
        }catch(java.sql.SQLException iS){
            System.out.println("Error importing...");
            System.out.println(iS.getMessage());
            throw new RuntimeException();
        }
        return 0;
    }
    //Tested
    public void purgeTable(String tableName) {
        String delAllEntryStr = "TRUNCATE TABLE " + tableName;
        try {

            PreparedStatement delStm = connection.prepareCall(delAllEntryStr);
            //delStm.setString(1, tableName);
            delStm.executeUpdate();
            delStm.close();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void commit(){
        try{
            connection.commit();
        }catch(SQLException e){
            throw new RuntimeException();
        }

    }
    public void rollBack(){
        try{
            connection.rollback();
        }catch(SQLException e){
            throw new RuntimeException();
        }
    }
    public void autoCommit(boolean isOn){
        try{
            connection.setAutoCommit(isOn);
        }catch(SQLException e){
            throw new RuntimeException();
        }
    }

    Set<ServiceData> getAllServiceRequestData(){
        Statement stm = null;
        try{
            stm = connection.createStatement();
        }catch(java.sql.SQLException e){
            System.out.println(e.getMessage());
        }
        String allString = "SELECT * FROM SERVICES";
        ResultSet rset = null;
        try {
            rset = stm.executeQuery(allString);
        }catch(java.sql.SQLException state){
            System.out.println(state.getMessage());
            state.printStackTrace();
        }
        Set<ServiceData> serviceSet = parseServiceResultSet(rset);
        try{
            rset.close();
        }catch(java.sql.SQLException e){
            System.out.println(e.getMessage());
            e.printStackTrace();
            throw new RuntimeException();
        }
        return serviceSet;
    }
    Set<ServiceData> parseServiceResultSet(ResultSet rset){
        Set<ServiceData> serviceSet = new HashSet<>();
        int serviceID;
        String serviceType;
        String status;
        String message;
        int assignedEmployee;
        Date dateCreated;
        String locationNode;

        try {
            while (rset.next()) {
                serviceID = rset.getInt("serviceID");
                serviceType = rset.getString("serviceType");
                status = rset.getString("STATUS");
                message = rset.getString("message");
                assignedEmployee = rset.getInt("ASSIGNEDEMPLOYEE");
                dateCreated = rset.getDate("timecreated");
                locationNode = rset.getString("Location");

                serviceSet.add(new ServiceData(serviceID,serviceType,status,message,assignedEmployee,dateCreated,locationNode));
            }

        } catch (java.sql.SQLException rsetFailure) {
            System.out.println(rsetFailure.getMessage());
            rsetFailure.printStackTrace();
            throw new RuntimeException(rsetFailure);
        }

        return serviceSet;


    }
    //Tested
    //  Package-private. Public method should take a ServiceRequest, and use the
    //  visitor pattern to save the correct concrete service-request type.
    void addServiceRequestData(ServiceData sd) {
        String addEntryStr = "INSERT INTO SERVICES (SERVICETYPE, STATUS, MESSAGE, ASSIGNEDEMPLOYEE, TIMECREATED, LOCATION) VALUES (?, ?, ?, ?, ?, ?)";
        try {
            PreparedStatement addStm = connection.prepareCall(addEntryStr);
            Date currentDate = new Date(Instant.now().toEpochMilli());
            addStm.setString(1,sd.getServiceType());
            addStm.setString(2,sd.getStatus());
            addStm.setString(3,sd.getMessage());
            addStm.setInt(4,sd.getAssignedEmployeeID());
            addStm.setDate(5,currentDate);
            addStm.setString(6,sd.getServiceNode());
            addStm.execute();
            addStm.close();
        }catch(SQLException e){
            System.out.println("Failed to add service Request");
            throw new RuntimeException();
        }
    }
    void updateServiceData(ServiceData sd){
        String updateStr = "UPDATE SERVICES SET STATUS = ?, MESSAGE = ?, ASSIGNEDEMPLOYEE = ?, LOCATION = ? WHERE SERVICEID = ?";
        PreparedStatement stm = null;
        try{
            stm = connection.prepareStatement(updateStr);
            stm.setString(1,sd.getStatus());
            stm.setString(2,sd.getMessage());
            stm.setInt(3,sd.getAssignedEmployeeID());
            stm.setString(4,sd.getServiceNode());
            stm.setInt(5,sd.getServiceID());
            stm.executeUpdate();

        }catch(java.sql.SQLException e){
            System.out.println(e.getMessage());
            throw new RuntimeException();
        }
    }
    void deleteServiceWithId(int id){
        String delStr = "DELETE FROM SERVICES WHERE SERVICEID = ?";
        PreparedStatement stm = null;
        try{
            stm = connection.prepareStatement(delStr);
            stm.setInt(1,id);
            stm.executeUpdate();

        }catch(java.sql.SQLException e){
            System.out.println(e.getMessage());
            throw new RuntimeException();
        }
    }

    public void addServiceRequest(ServiceRequest request) {
        if (request == null) ThrowHelper.illegalNull("request");

        AddServiceVisitor visitor = new AddServiceVisitor();

        //  When ServiceRequest.accept() is called, visit() will be called on the ServiceVisitor. What's more,
        //  the correct overload of visit() will be called.
        request.accept(visitor);
    }

    //  Each time you need to perform a specific operation differently for different subclasses
    //  of ServiceRequest, you can subclass ServiceVisitor.
    //  The base ServiceVisitor class must have an abstract method for each subclass of ServiceRequest.
    private final class AddServiceVisitor extends ServiceVisitor {
        //  Code that is specific to each particular type of service request should go in
        //  here. When you call accept() on a service request instance, it'll call visit() and
        //  pass itself as the argument. Because of the way overload resolution works, the correct overload
        //  will be called.
        @Override
        public void visit(JanitorServiceRequest request) {
            throw new NotImplementedException();
        }
        @Override
        public void visit(RideServiceRequest request) {
            throw new NotImplementedException();
        }
    }
}
