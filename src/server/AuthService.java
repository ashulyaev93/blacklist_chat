package Lesson_6.server;

import java.sql.*;

public class AuthService {

    private static Connection connection;
    private static Statement stmt;

    public static void connect() throws SQLException {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:userDB.db");//добавил ещё одну таблицу;
            stmt = connection.createStatement();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static String getNickByLoginAndPass(String login, String pass) throws SQLException {
        String sql = String.format("SELECT nickname FROM main where " +
                "login = '%s' and password = '%s'", login, pass);
        ResultSet rs = stmt.executeQuery(sql);

        if (rs.next()) {
            return rs.getString(1);
        }

        return null;
    }

    public static int getIdByNick(String nick) throws SQLException {
        String sql = String.format("SELECT id FROM main where " +
                "nickname = '%s'",
                nick);
        ResultSet rs = stmt.executeQuery(sql);

        if (rs.next()) {
            return rs.getInt(1);
        }

        return 0;
    }

    public static boolean checkBlockedList(String blockedNick, String nick) throws SQLException {
        String sql = String.format(
                "SELECT m.nickname AS blocked " +
                "FROM blocked_list bl " +
                "LEFT JOIN main m ON m.id = bl.u_bl_id " +
                "LEFT JOIN main m1 ON m1.id = bl.u_id " +
                "WHERE m.nickname='%s' AND m1.nickname = '%s'",
                blockedNick, nick);//новое условие вызова из SQL;

        ResultSet rs = stmt.executeQuery(sql);

        if (rs.next()) {
            return true;
        }

        return false;
    }

    public static void addBlockedList(int blockedId, int id) throws SQLException {
        String sql = String.format(
                "INSERT INTO blocked_list(u_id,u_bl_id) VALUES(%d,%d) " ,
                blockedId, id);//новое условие вызова из SQL;

        stmt.executeUpdate(sql);

    }

    public static void removeBlockedList(int blockedId, int id) throws SQLException {
        String sql = String.format(
                "DELETE FROM blocked_list WHERE u_bl_id = %d AND u_id = %d",
                id, blockedId);//новое условие удаления из SQL;

        stmt.executeUpdate(sql);

    }

//    public static String getU_Bl_IdbyId(String id) throws SQLException {
//        String sql = String.format("SELECT u_bl_id FROM blocked_list where id = '%s'", id);//новое условие вызова из SQL;
//        ResultSet rs = stmt.executeQuery(sql);
//
//        if (rs.next()) {
//            return rs.getString(1);
//        }
//
//        return null;
//    }

    public static void disconnect() {
        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
