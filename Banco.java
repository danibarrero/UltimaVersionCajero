import java.util.Scanner;
import java.sql.*;
import java.time.*;
import java.util.Scanner;
public class Banco {
        static Scanner sc = new Scanner(System.in);

        public static void main(String[] args) {
            Scanner scanner = new Scanner(System.in);
            String dni, consulta, nombre, apellidos, cuenta;
            double saldo = 0, importe_total = 0;
            LocalDateTime fecha;
            Connection connection;
            Statement st;
            ResultSet rs;

            System.setProperty("jdbc.drivers", "com.mysql.cj.jdbc.Driver");
            String url = "jdbc:mysql://localhost:3306/cuentas_bancarias";
            String user = "root";
            String pass = "root";
            int opcion;
            try {
                connection = DriverManager.getConnection(url, user, pass);
                System.out.println("Conexión exitosa.");

                do {
                    System.out.println("\nCajero Automático");
                    System.out.println("1- Retirar fondos");
                    System.out.println("2- Ingresar fondos");
                    System.out.println("3- Consultar movimientos");
                    System.out.println("4- Listar todas las cuentas de un cliente");
                    System.out.println("5- Consultar cuentas con saldo menor a una cantidad");
                    System.out.println("0- Salir");
                    System.out.print("Seleccione una opción: ");
                    opcion = scanner.nextInt();

                    switch (opcion) {
                        case 1: // Retirar fondos
                            System.out.println("Introduzca la cuenta: ");
                            cuenta = scanner.next();
                            System.out.print("Ingrese la cantidad a retirar: ");
                            Double importe = scanner.nextDouble();
                            if (verificarSaldoSuficiente(connection, cuenta, importe)) {
                                fecha = LocalDateTime.now();
                                importe *= -1;
                                consulta = "insert into movimientos values ('" + cuenta + "', '" + fecha + "' , " + importe + ")";
                                st = connection.createStatement();
                                st.executeUpdate(consulta);
                            } else {
                                System.out.println("Saldo insuficiente. No se puede retirar la cantidad especificada.");
                            }
                            break;
                        case 2: // Ingresar fondos
                            System.out.println("Introduzca la cuenta: ");
                            cuenta = scanner.next();
                            System.out.print("Ingrese la cantidad a depositar: ");
                            importe = scanner.nextDouble();
                            fecha = LocalDateTime.now();
                            consulta = "insert into movimientos values ('" + cuenta + "', '" + fecha + "' , " + importe + ")";
                            st = connection.createStatement();
                            st.executeUpdate(consulta);
                            break;
                        case 3: // Consultar movimientos
                            System.out.print("Introduzca el Número de Cuenta : ");
                            cuenta = scanner.next();
                            consultarMovimientos(connection, cuenta);
                            break;
                        case 4: // Listar todas las cuentas de un cliente
                            System.out.print("Introduzca el DNI del cliente: ");
                            dni = scanner.next();
                            listarCuentasCliente(connection, dni);
                            break;
                        case 5: // Consultar cuentas con saldo menor a una cantidad
                            System.out.print("Ingrese la cantidad límite de saldo: ");
                            double limiteSaldo = scanner.nextDouble();
                            consultarCuentasSaldoMenor(connection, limiteSaldo);
                            break;
                        case 0: // Salir
                            System.out.println("Saliendo del sistema.");
                            break;
                        default:
                            System.out.println("Opción no válida. Intente nuevamente.");
                    }
                } while (opcion != 0);

                scanner.close();

            } catch (SQLException sqle) {
                System.out.println(sqle.getMessage());
            }
        }

        private static boolean verificarSaldoSuficiente(Connection connection, String cuenta, double importe) throws SQLException {
            String saldoQuery = "SELECT saldo FROM cuentas WHERE num_cuenta = '" + cuenta + "'";
            Statement st = connection.createStatement();
            ResultSet rs = st.executeQuery(saldoQuery);

            if (rs.next()) {
                double saldoActual = rs.getDouble("saldo");
                return (saldoActual + importe) >= 0;
            }
            return false;
        }

        private static void consultarMovimientos(Connection connection, String cuenta) throws SQLException {
            String consulta = "SELECT num_cuenta, fecha, importe FROM movimientos WHERE num_cuenta = '" + cuenta + "'";
            Statement st = connection.createStatement();
            ResultSet rs = st.executeQuery(consulta);
            System.out.println("Cuenta Fecha Importe");
            System.out.println("-------------------- ------------------- ------------- ");
            while (rs.next()) {
                cuenta = rs.getString("num_cuenta");
                Date dia = rs.getDate("fecha");
                Time hora = rs.getTime("fecha");
                Double importe = (double) rs.getFloat("importe");
                System.out.println(cuenta + " " + dia + " " + hora + " " + importe);
            }
        }

        private static void listarCuentasCliente(Connection connection, String dni) throws SQLException {
            String consulta = "SELECT num_cuenta FROM cuentas WHERE dni_cliente = '" + dni + "'";
            Statement st = connection.createStatement();
            ResultSet rs = st.executeQuery(consulta);
            System.out.println("Cuentas del cliente " + dni);
            System.out.println("--------------------");
            while (rs.next()) {
                String numCuenta = rs.getString("num_cuenta");
                System.out.println(numCuenta);
            }
        }

        private static void consultarCuentasSaldoMenor(Connection connection, double limiteSaldo) throws SQLException {
            String consulta = "SELECT c.num_cuenta, c.saldo, m.fecha, m.importe, cl.nombre, cl.apellidos, cl.dni FROM cuentas c " +
                    "JOIN movimientos m ON c.num_cuenta = m.num_cuenta " +
                    "JOIN clientes cl ON c.dni_cliente = cl.dni " +
                    "WHERE c.saldo < " + limiteSaldo;
            Statement st = connection.createStatement();
            ResultSet rs = st.executeQuery(consulta);
            System.out.println("Cuenta Saldo Fecha Importe Cliente");
            System.out.println("------ ----- -------------------- ------------- --------------------");
            while (rs.next()) {
                String numCuenta = rs.getString("num_cuenta");
                double saldo = rs.getDouble("saldo");
                Date dia = rs.getDate("fecha");
                Time hora = rs.getTime("fecha");
                Double importe = (double) rs.getFloat("importe");
                String nombre = rs.getString("nombre");
                String apellidos = rs.getString("apellidos");
                String dniCliente = rs.getString("dni");
                System.out.println(numCuenta + " " + saldo + " " + dia + " " + hora + " " + importe + " " + nombre + " " + apellidos + " " + dniCliente);
            }
        }
    }
