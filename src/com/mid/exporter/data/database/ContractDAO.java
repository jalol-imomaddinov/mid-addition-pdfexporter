package com.mid.exporter.data.database;

import com.mid.exporter.data.common.AmoundInfo;
import com.mid.exporter.data.common.StateType;
import com.mid.exporter.data.model.FullContractModel;
import java.sql.SQLException;

/**
 * @author
 */
public interface ContractDAO {

    public void initContractReader(int current, int max) throws SQLException;

    public FullContractModel readFullContract(int id, FullContractModel model) throws SQLException;

    public void updateContract(FullContractModel model) throws SQLException;

    public void saveContract(FullContractModel model) throws SQLException;

    public void setCriteria(String criteria);

    public void refreshAmoundInfo() throws SQLException;
    
    public AmoundInfo getAmoundInfo() throws SQLException;
    
    public int getContractCount() throws SQLException;

    public double[] getWorkersPay(int id) throws SQLException;
}
