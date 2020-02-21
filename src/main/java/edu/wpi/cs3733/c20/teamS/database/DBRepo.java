package edu.wpi.cs3733.c20.teamS.database;


import java.util.Set;

public interface DBRepo {
    void addNode(NodeData nd);
    void addSetOfNodes(Set<NodeData> set);
    Set<NodeData> getAllNodes();
    NodeData getNode(String ID);
    void removeNode(String nodeID);

    Set<EdgeData> getAllEdges();
    void addEdge(EdgeData edge);
    void removeEdge(String edgeID);

    void importStartUpData();
    void purgeTable(String tableName);
    void commit();
    void rollBack();
    void autoCommit(boolean isOn);

    void addEmployee(EmployeeData ed);
    boolean checkLogin(String username, String password);
    EmployeeData getEmployee(String username);
    void removeEmployee(String username);
    void updateEmployee(EmployeeData emp);

    void addServiceRequestData(ServiceData sd);
    void updateServiceData(ServiceData sd);
    void deleteServiceWithId(int id);

    Set<Integer> getCapableEmployees(String serviceType);
    void addCapability(int ID, String type);
    void removeCapability(int ID, String type);


}
