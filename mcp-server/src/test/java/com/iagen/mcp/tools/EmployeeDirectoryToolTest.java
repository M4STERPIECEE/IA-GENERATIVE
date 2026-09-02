package com.iagen.mcp.tools;

import com.iagen.mcp.security.OutputSanitizer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;

@ExtendWith(MockitoExtension.class)
class EmployeeDirectoryToolTest {

    @Mock
    private OutputSanitizer sanitizer;

    private EmployeeDirectoryTool tool;

    @BeforeEach
    void setUp() throws Exception {
        tool = new EmployeeDirectoryTool(sanitizer);
        org.mockito.Mockito.lenient().when(sanitizer.sanitize(anyString(), eq("EmployeeDirectoryTool"))).thenAnswer(inv -> inv.getArgument(0));

        List<Employee> testEmployees = new ArrayList<>();
        testEmployees.add(new Employee("1", "Alice Martin", "Finance", "Paris", "alice@iagen.fr"));
        testEmployees.add(new Employee("2", "Bob Dupont", "Informatique", "Lyon", "bob@iagen.fr"));
        testEmployees.add(new Employee("3", "Claire Leroy", "Ressources Humaines", "Paris", "claire@iagen.fr"));
        testEmployees.add(new Employee("4", "David Bernard", "Finance", "Bordeaux", "david@iagen.fr"));

        Field employeesField = EmployeeDirectoryTool.class.getDeclaredField("employees");
        employeesField.setAccessible(true);
        ((List<Employee>) employeesField.get(tool)).addAll(testEmployees);
    }

    @Test
    void searchEmployeeDirectory_byName() {
        String result = tool.searchEmployeeDirectory("Alice");

        assertThat(result).contains("Alice Martin");
        assertThat(result).contains("Finance");
        assertThat(result).contains("alice@iagen.fr");
    }

    @Test
    void searchEmployeeDirectory_byDepartment() {
        String result = tool.searchEmployeeDirectory("Finance");

        assertThat(result).contains("Alice Martin");
        assertThat(result).contains("David Bernard");
        assertThat(result).contains("2 résultat(s)");
    }

    @Test
    void searchEmployeeDirectory_byCity() {
        String result = tool.searchEmployeeDirectory("Paris");

        assertThat(result).contains("Alice Martin");
        assertThat(result).contains("Claire Leroy");
        assertThat(result).doesNotContain("Bob Dupont");
    }

    @Test
    void searchEmployeeDirectory_caseInsensitive() {
        String result = tool.searchEmployeeDirectory("informatique");

        assertThat(result).contains("Bob Dupont");
    }

    @Test
    void searchEmployeeDirectory_noResult() {
        String result = tool.searchEmployeeDirectory("Mars");

        assertThat(result).contains("Aucun employé trouvé");
        assertThat(result).contains("Mars");
    }

    @Test
    void searchEmployeeDirectory_blankQuery() {
        String result = tool.searchEmployeeDirectory("   ");

        assertThat(result).contains("Veuillez fournir un terme de recherche");
    }

    @Test
    void searchEmployeeDirectory_nullQuery() {
        String result = tool.searchEmployeeDirectory(null);

        assertThat(result).contains("Veuillez fournir un terme de recherche");
    }

    @Test
    void searchEmployeeDirectory_singleResult() {
        String result = tool.searchEmployeeDirectory("Bob");

        assertThat(result).contains("1 résultat(s)");
        assertThat(result).contains("Bob Dupont");
        assertThat(result).contains("Informatique");
        assertThat(result).contains("Lyon");
    }

    @Test
    void searchEmployeeDirectory_partialNameMatch() {
        String result = tool.searchEmployeeDirectory("Bernard");

        assertThat(result).contains("David Bernard");
        assertThat(result).doesNotContain("Alice Martin");
    }

    @Test
    void searchEmployeeDirectory_callsSanitizer() {
        tool.searchEmployeeDirectory("Paris");

        org.mockito.Mockito.verify(sanitizer).sanitize(anyString(), eq("EmployeeDirectoryTool"));
    }
}
