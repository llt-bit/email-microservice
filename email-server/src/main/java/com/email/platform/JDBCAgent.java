package com.email.platform;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.jdbc.Work;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * JDBCAgent 兼容类 —— 对齐 OA com.seeyon.ctp.util.JDBCAgent。
 * 底层走 Hibernate Session.doWork 或直接 JDBC。
 */
public class JDBCAgent implements AutoCloseable {

    private boolean autoCommit;
    private boolean closeConnection;
    private Connection connection;

    public JDBCAgent() {
        this(false, false);
    }

    public JDBCAgent(boolean autoCommit, boolean closeConnection) {
        this.autoCommit = autoCommit;
        this.closeConnection = closeConnection;
    }

    /**
     * OA JDBCAgent.execute(sql, params) — 用 PreparedStatement 执行 SQL。
     */
    public int execute(String sql, List<Object> params) throws Exception {
        SessionFactory sf = DBAgent.getSessionFactorySnapshot();
        if (sf != null) {
            final String fSql = sql;
            final List<Object> fParams = params;
            final int[] result = new int[1];
            Session session = sf.getCurrentSession();
            session.doWork(new Work() {
                @Override
                public void execute(Connection conn) throws SQLException {
                    try (PreparedStatement ps = conn.prepareStatement(fSql)) {
                        if (fParams != null) {
                            for (int i = 0; i < fParams.size(); i++) {
                                ps.setObject(i + 1, fParams.get(i));
                            }
                        }
                        result[0] = ps.executeUpdate();
                    }
                }
            });
            return result[0];
        }
        return 0;
    }

    public Connection getConnection() throws Exception {
        if (connection == null) {
            SessionFactory sf = DBAgent.getSessionFactorySnapshot();
            if (sf != null) {
                // Get connection from Hibernate session
                final Connection[] conns = new Connection[1];
                sf.getCurrentSession().doWork(new Work() {
                    @Override
                    public void execute(Connection conn) throws SQLException {
                        conns[0] = conn;
                    }
                });
                connection = conns[0];
            }
        }
        return connection;
    }

    @Override
    public void close() {
        if (closeConnection && connection != null) {
            try { connection.close(); } catch (Exception e) {}
        }
    }

    /** 单参数版 execute(sql) */
    public int execute(String sql) throws Exception {
        return execute(sql, Collections.emptyList());
    }

    /** OA JDBCAgent.resultSetToList */
    public static List<List<Object>> resultSetToList(ResultSet rs) throws SQLException {
        List<List<Object>> result = new ArrayList<>();
        int colCount = rs.getMetaData().getColumnCount();
        while (rs.next()) {
            List<Object> row = new ArrayList<>();
            for (int i = 1; i <= colCount; i++) {
                row.add(rs.getObject(i));
            }
            result.add(row);
        }
        return result;
    }

    /** OA JDBCAgent.resultSetToMap */
    public static List<Map<String, Object>> resultSetToMap(ResultSet rs) throws SQLException {
        List<Map<String, Object>> result = new ArrayList<>();
        int colCount = rs.getMetaData().getColumnCount();
        while (rs.next()) {
            Map<String, Object> row = new LinkedHashMap<>();
            for (int i = 1; i <= colCount; i++) {
                row.put(rs.getMetaData().getColumnName(i), rs.getObject(i));
            }
            result.add(row);
        }
        return result;
    }

    /** OA JDBCAgent.isTableExist */
    public static boolean isTableExist(String tableName) { return true; }

    /** OA JDBCAgent.getSqlInFile */
    public static String getSqlInFile(String path) { return ""; }
}
