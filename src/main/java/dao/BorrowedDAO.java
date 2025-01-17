package dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import models.Borrowed;
import models.Employee;

public class BorrowedDAO {
    private final Connection connection;
    public BorrowedDAO(Connection connection) { this.connection = connection; }

    public void create(Borrowed borrowed) throws SQLException {
        String sql = "INSERT INTO borrowed (IDEMPLOYEE, IDEQUIPMENT, DATE, SUPPLIERID) "
                + "VALUES (?, ?, ?, ?) "
                + "ON DUPLICATE KEY UPDATE IDEMPLOYEE=VALUES(IDEMPLOYEE), "
                + "DATE=VALUES(DATE), SUPPLIERID=VALUES(SUPPLIERID)";

        PreparedStatement pstm = connection.prepareStatement(sql);
        pstm.setInt(1, borrowed.getIdEmployee());
        pstm.setInt(2, borrowed.getIdEquipment());
        pstm.setDate(3, borrowed.getDate());
        pstm.setInt(4, borrowed.getSupplierId());

        pstm.execute();
        pstm.close();
    }

    public List<Borrowed> listBorrowed(Integer id) throws SQLException {
        String sql = "SELECT equipments.name, borrowed.idEquipment, borrowed.date, sup.name " +
                "FROM equipments " +
                "INNER JOIN borrowed " +
                "ON equipments.idEquipment = borrowed.idEquipment AND equipments.supplierId = borrowed.supplierId " +
                "JOIN supplier sup ON equipments.supplierId = sup.supplierId WHERE borrowed.idEmployee = ?";

        PreparedStatement pstm = connection.prepareStatement(sql);
        pstm.setInt(1, id);
        pstm.execute();
        ResultSet rst = pstm.getResultSet();
        List<Borrowed> borrowings = new ArrayList<>();

        while  (rst.next()) {
            String name = rst.getString(1);
            Integer idEquip = rst.getInt(2);
            Date date = rst.getDate(3);
            String supplierName = rst.getString(4);

            borrowings.add(new Borrowed(name,idEquip,date, supplierName));
        }
        pstm.close();
        rst.close();

        return borrowings;
    }

    public void readName(String name) throws SQLException { //FAZER INNER JOIN PARA USAR NOME DO FUNCIONÁRIO!
        String sql = "SELECT * FROM borrowed WHERE NAME LIKE ?";

        PreparedStatement pstm = connection.prepareStatement(sql);
        pstm.setString(1, "%" + name + "%");

        pstm.execute();

        ResultSet rst = pstm.getResultSet();
        while (rst.next()) {
            Borrowed borrowed = new Borrowed(rst.getInt(1),rst.getInt(2), rst.getDate(3), rst.getInt(4));
        }

        pstm.close();
        rst.close();
    }

    public Borrowed readId(Integer id) throws SQLException{
        String sql = "SELECT * FROM borrowed WHERE IDEQUIPMENT = ?";

        PreparedStatement pstm = connection.prepareStatement(sql);
        pstm.setInt(1, id);

        pstm.execute();

        ResultSet rst = pstm.getResultSet();

        if (rst.next()) {
            Borrowed borrowed = new Borrowed(rst.getInt(1), rst.getInt(2), rst.getDate(3), rst.getInt(4));
            pstm.close();
            rst.close();
            return borrowed;
        }

        pstm.close();
        rst.close();
        return null;
    }

    public void delete(Integer idEquip, Integer supplierId) throws SQLException {

        String sql = "DELETE FROM borrowed WHERE IDEQUIPMENT = ? AND supplierId = ?";

        PreparedStatement pstm = connection.prepareStatement(sql);
        pstm.setInt(1, idEquip);
        pstm.setInt(2, supplierId);

        Integer rows = pstm.executeUpdate();

        pstm.close();
    }
    public int getSupplierId(String name) throws SQLException{
        String sql = "SELECT supplierId FROM supplier WHERE name = ?";
        PreparedStatement pstm = connection.prepareStatement(sql);
        pstm.setString(1, name);
        pstm.execute();

        ResultSet rst = pstm.getResultSet();

        if(rst.next()){
            return rst.getInt(1);
        }
        return 0;
    }
    public Boolean searchBorrowed(Integer EquipmentId, Integer supplierId) throws SQLException {
        String sql = "SELECT * FROM borrowed WHERE idEquipment = ? AND supplierId = ?";
        PreparedStatement pstm = connection.prepareStatement(sql);
        pstm.setInt(1, EquipmentId);
        pstm.setInt(2, supplierId);
        pstm.execute();

        ResultSet rst = pstm.getResultSet();

        if (rst.next()) {
            return true;
        }
        return false;
    }
}
