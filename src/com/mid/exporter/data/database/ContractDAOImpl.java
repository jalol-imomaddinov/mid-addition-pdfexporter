package com.mid.exporter.data.database;

import com.mid.exporter.data.common.AmoundInfo;
import com.mid.exporter.data.common.ProductType;
import com.mid.exporter.data.common.StateType;
import com.mid.exporter.data.model.FullContractModel;
import com.mid.exporter.util.DateUtil;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;

public class ContractDAOImpl implements ContractDAO {

    private final static String AMOUNDS_QUERY = "SELECT "
	    + "sum(agreed_amound),"
	    + " sum(prepaid_amound),"
	    + " sum((SELECT sum(amound) AS payaments FROM Payements WHERE order_id = Orders.order_id))"
	    + " from Orders where fused = 0;";
    
    private final static String FILTERED_INIT_QUERY_LEFT = "SELECT order_id, owner_name, catalog, width, height, "
	    + "agreed_amound, prepaid_amound, "
	    + "(SELECT sum(amound) FROM Payements WHERE order_id = Orders.order_id) AS sum, product_type,"
	    + "limit_end, state, fused, number, "
	    + "(SELECT group_concat(Workers.name, ', ') FROM BindedWorkers inner join Workers on Workers.worker_id = BindedWorkers.worker_id WHERE (BindedWorkers.order_id = Orders.order_id and job = 0)) AS welders, "
	    + "(SELECT group_concat(Workers.name, ', ') FROM BindedWorkers inner join Workers on Workers.worker_id = BindedWorkers.worker_id WHERE (BindedWorkers.order_id = Orders.order_id and job = 1)) AS painters "
	    + "FROM Orders ";
    
    private final static String FILTERED_INIT_QUERY_RIGHT = " ORDER BY Orders.order_id DESC limit ?,?;";
    
    private final static String INIT_QUERY = "SELECT order_id, owner_name, catalog, width, height, "
	    + "agreed_amound, prepaid_amound, "
	    + "(SELECT sum(amound) FROM Payements WHERE order_id = Orders.order_id) AS sum, product_type,"
	    + "limit_end, state, number, "
	    + "(SELECT group_concat(Workers.name, ', ') FROM BindedWorkers inner join Workers on Workers.worker_id = BindedWorkers.worker_id WHERE (BindedWorkers.order_id = Orders.order_id and job = 0)) AS welders, "
	    + "(SELECT group_concat(Workers.name, ', ') FROM BindedWorkers inner join Workers on Workers.worker_id = BindedWorkers.worker_id WHERE (BindedWorkers.order_id = Orders.order_id and job = 1)) AS painters "
	    + "FROM Orders WHERE fused = 0"
	    + " ORDER BY Orders.order_id DESC limit ?,?;";

    private final String CONTRACT_UPDATE = "UPDATE Orders SET number = ?, catalog = ?, product_type = ?,  "
	    + "width = ?, height = ?, owner_name = ?, owner_number = ?, limit_start = ?, limit_end = ?, "
            + "source_amound = ?, agreed_amound = ?, prepaid_amound = ?, "
	    + "note = ?, square_price = ?, welder_pay = ?, painter_pay = ?, "
	    + "welder_sqr_price = ?, painter_sqr_price = ?, binded_worker = ? "
	    + "WHERE order_id = ?;";

    private final String CONTRACT_REMOVE = "UPDATE Orders SET fused = 1 where order_id = ?;";

    private final String CONTRACT_INSERT = "INSERT INTO Orders (number, catalog, product_type, "
            + "width, height, owner_name, owner_number, "
	    + "limit_start, limit_end, "
            + "source_amound, agreed_amound, prepaid_amound, "
            + "note, square_price, welder_pay, painter_pay, "
	    + "welder_sqr_price, painter_sqr_price,  binded_worker, state, fused) "
	    + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 4, 0);";

    private final static String REFRESH_QUERY = "SELECT Orders.order_id, owner_name, catalog, width, height, "
	    + "agreed_amound, prepaid_amound, "
	    + "(SELECT sum(amound) FROM Payements WHERE order_id = Orders.order_id) AS sum, "
	    + "limit_end, state, fused, number, "
	    + "(SELECT group_concat(Workers.name, ', ') FROM BindedWorkers inner join Workers on Workers.worker_id = BindedWorkers.worker_id WHERE (BindedWorkers.order_id = Orders.order_id and job = 0)) AS welders, "
	    + "(SELECT group_concat(Workers.name, ', ') FROM BindedWorkers inner join Workers on Workers.worker_id = BindedWorkers.worker_id WHERE (BindedWorkers.order_id = Orders.order_id and job = 1)) AS painters "
	    + "FROM Orders "
	    + " WHERE Orders.order_id = ?;";

    private PreparedStatement statement;
    private ResultSet resultSet;
    private String criteria;

    private AmoundInfo amoundInfo;
    
    private void initAmoundInfo() throws SQLException {
	long t = System.currentTimeMillis();
	PreparedStatement stmt = DatabaseHandler.getDataConnection().prepareStatement(AMOUNDS_QUERY);
	ResultSet result = stmt.executeQuery();
	double agreed = result.getDouble(1);
	double prepaid = result.getDouble(2);
	double payements = result.getDouble(3);
	stmt.close();

	amoundInfo.setAgreed(agreed);
	amoundInfo.setPrepaid(prepaid + payements);
	
	System.out.println("initAmoundInfo time: " + (System.currentTimeMillis() - t) + "ms");
    }
    
    @Override
    public void initContractReader(int current, int max) throws SQLException {

	String query;
	if (criteria != null) {
	    query = FILTERED_INIT_QUERY_LEFT + criteria + FILTERED_INIT_QUERY_RIGHT;
	    System.out.println(query);
	}
	else {
	    query = INIT_QUERY;
	}
	statement = DatabaseHandler.getDataConnection().prepareStatement(query);

	statement.setInt(1, current * max);
	statement.setInt(2, max);

	resultSet = statement.executeQuery();
    }

    @Override
    public FullContractModel readFullContract(int id, FullContractModel model) throws SQLException {

	PreparedStatement fullStatement = DatabaseHandler.getDataConnection().prepareStatement("select * from Orders where order_id = ?;");
	fullStatement.setInt(1, id);

	ResultSet result = fullStatement.executeQuery();

	model.setId(result.getInt(1));
	model.setNumber(result.getInt(2));

	model.setCatalog(result.getString(3));

	model.setProductType(ProductType.productById(result.getInt(4)));

	model.setWidth(result.getDouble(5));
	model.setHeight(result.getDouble(6));

	model.setOwnerName(result.getString(7));
	model.setOwnerNumber(result.getString(8));

	LocalDate limitStart = DateUtil.dbParse(result.getString(9));
	model.setLimitStart(limitStart);

	LocalDate limitEnd = DateUtil.dbParse(result.getString(10));
	model.setLimitEnd(limitEnd);

	model.setSourceAmound(result.getDouble(11));
	model.setAgreedAmound(result.getDouble(12));
	model.setPrepaidAmound(result.getDouble(13));
	model.setRemaindAmound(model.getAgreedAmound() - model.getPrepaidAmound());

	model.setNote(result.getString(14));

	model.setSquarePrice(result.getDouble(15));
	model.setWelderPay(result.getDouble(16));
	model.setPainterPay(result.getDouble(17));
	model.setWelderSqrPay(result.getDouble(18));
	model.setPainterSqrPay(result.getDouble(19));
        model.setState(StateType.getStateById(result.getInt(20)));
        model.setFuse(result.getInt(21) == 0);
	model.setBindedWoker(result.getInt(22));

        for (int i = 1; i <= 22; i++) {
            System.out.println(i + ": " + result.getString(i));
        }
        
	return model;
    }

    @Override
    public void updateContract(FullContractModel model) throws SQLException {
	PreparedStatement stmt = DatabaseHandler.getDataConnection().prepareStatement(CONTRACT_UPDATE);

	stmt.setInt(1, model.getNumber());
	stmt.setString(2, model.getCatalog());
	stmt.setInt(3, model.getProductType().getId());

	stmt.setDouble(4, model.getWidth());
	stmt.setDouble(5, model.getHeight());

	stmt.setString(6, model.getOwnerName());
	stmt.setString(7, model.getOwnerNumber());

	stmt.setString(8, DateUtil.dbFormat(model.getLimitStart()));
	stmt.setString(9, DateUtil.dbFormat(model.getLimitEnd()));

	stmt.setDouble(10, model.getSourceAmound());
	stmt.setDouble(11, model.getAgreedAmound());
	stmt.setDouble(12, model.getPrepaidAmound());

	stmt.setString(13, model.getNote());

	stmt.setDouble(14, model.getSquarePrice());
	stmt.setDouble(15, model.getWelderPay());
	stmt.setDouble(16, model.getPainterPay());
	stmt.setDouble(17, model.getWelderSqrPay());
	stmt.setDouble(18, model.getPainterSqrPay());

	stmt.setInt(19, model.getBindedWoker());
	stmt.setInt(20, model.getId());

	boolean execute = stmt.execute();
	initAmoundInfo();
    }

    @Override
    public void saveContract(FullContractModel model) throws SQLException {
	PreparedStatement stmt = DatabaseHandler.getDataConnection().prepareStatement(CONTRACT_INSERT);

	stmt.setInt(1, model.getNumber());
	stmt.setString(2, model.getCatalog());
	stmt.setInt(3, model.getProductType().getId());

	stmt.setDouble(4, model.getWidth());
	stmt.setDouble(5, model.getHeight());

	stmt.setString(6, model.getOwnerName());
	stmt.setString(7, model.getOwnerNumber());

	stmt.setString(8, DateUtil.dbFormat(model.getLimitStart()));
	stmt.setString(9, DateUtil.dbFormat(model.getLimitEnd()));

	stmt.setDouble(10, model.getSourceAmound());
	stmt.setDouble(11, model.getAgreedAmound());
	stmt.setDouble(12, model.getPrepaidAmound());

	stmt.setString(13, model.getNote());

	stmt.setDouble(14, model.getSquarePrice());
	stmt.setDouble(15, model.getWelderPay());
	stmt.setDouble(16, model.getPainterPay());

	stmt.setDouble(17, model.getWelderSqrPay());
	stmt.setDouble(18, model.getPainterSqrPay());

	stmt.setInt(19, model.getBindedWoker());

	boolean execute = stmt.execute();

	stmt.close();

	Statement createStatement = DatabaseHandler.getDataConnection().createStatement();
	createStatement.execute("update DynamicData set next_contract = next_contract + 1;");
	createStatement.close();
	
	initAmoundInfo();
    }

    @Override
    public void refreshAmoundInfo() throws SQLException {
	initAmoundInfo();
    }

    @Override
    public AmoundInfo getAmoundInfo() throws SQLException {
	if (amoundInfo == null) {
	    amoundInfo = new AmoundInfo();
	    initAmoundInfo();
	}
	return amoundInfo;
    }

    @Override
    public int getContractCount() throws SQLException {
	PreparedStatement countStatement = DatabaseHandler.getDataConnection().prepareStatement("select count(*) from Orders;");
	ResultSet executeQuery = countStatement.executeQuery();

	int count = 0;

	if (executeQuery.next()) {
	    count = executeQuery.getInt(1);
	}

	executeQuery.close();
	countStatement.close();

	return count;
    }

    @Override
    public void setCriteria(String criteria) {
	this.criteria = criteria;
    }

    @Override
    public double[] getWorkersPay(int id) throws SQLException {
	double[] pays = new double[2];

	try (PreparedStatement stmt = DatabaseHandler.getDataConnection().prepareStatement("select welder_pay, painter_pay from Orders where order_id = ?;")) {
	    stmt.setInt(1, id);
	    ResultSet result = stmt.executeQuery();
	    pays[0] = result.getDouble(1);
	    pays[1] = result.getDouble(2);
	    result.close();
	}

	return pays;
    }
}
