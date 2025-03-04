<?php
// Initialize variables to hold the input from the user
$db_name = '';
$error = '';
$mysql_only_usernames = [];

// Check if the form is submitted
if ($_SERVER["REQUEST_METHOD"] == "POST") {
    // Get the database name from user input
    $db_name = $_POST['db_name'];

    // Define database credentials for MySQL and PostgreSQL
    $mysql_user = "read_user";
    $mysql_password = "Skolera_2022";
    $mysql_host = "172.31.164.142";

    $pg_user = "skolera";
    $pg_password = "8B3z45cTLnbuSTNWj5wq";
    $pg_host = "erp.cdekakey851s.eu-west-1.rds.amazonaws.com";

    // MySQL connection using mysqli
    $mysql_conn = new mysqli($mysql_host, $mysql_user, $mysql_password, $db_name);

    // Check MySQL connection
    if ($mysql_conn->connect_error) {
        $error = "MySQL connection failed: " . $mysql_conn->connect_error;
    } else {
        // PostgreSQL connection using pg_connect
        $pg_conn_string = "host=$pg_host dbname=$db_name user=$pg_user password=$pg_password";
        $pg_conn = pg_connect($pg_conn_string);

        // Check PostgreSQL connection
        if (!$pg_conn) {
            $error = "PostgreSQL connection failed: " . pg_last_error();
        } else {
            // Query MySQL to get usernames from the "users" table
            $mysql_result = $mysql_conn->query("SELECT username FROM users where deleted_at is null and user_type = 'student'");
            $mysql_usernames = [];
            if ($mysql_result->num_rows > 0) {
                while ($row = $mysql_result->fetch_assoc()) {
                    $mysql_usernames[] = $row['username']; // Store MySQL usernames in an array
                }
            }

            // Query PostgreSQL to get usernames from the "op_student" table
            $pg_result = pg_query($pg_conn, "SELECT username FROM op_student");
            $pg_usernames = [];
            if ($pg_result) {
                while ($row = pg_fetch_assoc($pg_result)) {
                    $pg_usernames[] = $row['username']; // Store PostgreSQL usernames in an array
                }
            }

            // Find usernames that are in MySQL but not in PostgreSQL
            $mysql_only_usernames = array_diff($mysql_usernames, $pg_usernames);

            // Close the connections
            $mysql_conn->close();
            pg_close($pg_conn);
        }
    }
}
?>

<!DOCTYPE html>
<html>
<head>
    <title>Database Comparison</title>
    <style>
        table {
            width: 50%;
            border-collapse: collapse;
            margin: 20px 0;
        }

        table, th, td {
            border: 1px solid black;
        }

        th, td {
            padding: 10px;
            text-align: left;
        }

        th {
            background-color: #f2f2f2;
        }

        .error {
            color: red;
        }
    </style>
</head>
<body>

<h2>Enter the Database Name (same for LMS and ERP)</h2>

<form method="POST" action="">
    <label for="db_name">LMS Code:</label><br>
    <input type="text" id="db_name" name="db_name" required><br><br>

    <input type="submit" value="Compare Databases">
</form>

<?php if (!empty($error)): ?>
    <p class="error"><?php echo $error; ?></p>
<?php endif; ?>

<?php if (!empty($mysql_only_usernames)): ?>
    <h2>Usernames in LMS but NOT in ERP</h2>
    <table>
        <tr>
            <th>Username</th>
        </tr>
        <?php foreach ($mysql_only_usernames as $username): ?>
            <tr>
                <td><?php echo htmlspecialchars($username); ?></td>
            </tr>
        <?php endforeach; ?>
    </table>
<?php elseif ($_SERVER["REQUEST_METHOD"] == "POST" && empty($mysql_only_usernames) && empty($error)): ?>
    <p>No unmatched usernames found.</p>
<?php endif; ?>

</body>
</html>
