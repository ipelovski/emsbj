package emsbj;

import emsbj.mark.Mark;
import emsbj.user.User;
import emsbj.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Component
@Order(1000000)
public class InitializationFilter implements Filter {
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private SchoolYearRepository schoolYearRepository;
    @Autowired
    private TermRepository termRepository;
    @Autowired
    private SubjectRepository subjectRepository;
    @Autowired
    private SubjectService subjectService;
    @Autowired
    private GradeRepository gradeRepository;
    @Autowired
    private StudentRepository studentRepository;
    @Autowired
    private WeeklySlotRepository weeklySlotRepository;
    private boolean initialized;
    private SchoolYear schoolYear;
    private Map<Integer, Grade> grades = new HashMap<>(4);
    private List<Subject> subjects = new LinkedList<>();

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        if (!initialized) {
            initialize();
        }
        filterChain.doFilter(servletRequest, servletResponse);
    }

    public void initialize() {
        User admin = new User("admin");
        admin.setRole(User.Role.admin);
        admin.setPassword(passwordEncoder.encode("admin"));
        userRepository.save(admin);
        JournalAuditAware.setCurrentUser(admin);
        createSchoolYear();
        createTerms();
        createGrades();
        createSubjects();
        createWeeklySlots();
        createStudents();
        initialized = true;
    }

    private void createSchoolYear() {
        SchoolYear schoolYear = new SchoolYear(2020, 2021);
        this.schoolYear = schoolYearRepository.save(schoolYear);
    }

    private void createTerms() {
        Term firstTerm = new Term(schoolYear, "I");
        termRepository.save(firstTerm);
        Term secondTerm = new Term(schoolYear, "II");
        termRepository.save(secondTerm);
    }

    private void createGrades() {
        for (Integer i = 9; i <= 12; i++) {
            Grade grade = new Grade(i);
            gradeRepository.save(grade);
            grades.put(i, grade);
        }
        {
            Grade grade = new Grade(8);
            gradeRepository.save(grade);
        }
        {
            Grade grade = new Grade("8 prep");
            gradeRepository.save(grade);
        }
    }

    private void createSubjects() {
        Subject subject = subjectService.create("Биология", grades.get(9));
        subjects.add(subject);
    }

    private void createWeeklySlots() {
        Duration lessonDuration = Duration.ofMinutes(40);
        Duration breakDuration = Duration.ofMinutes(10);
        Duration longBreakDuration = Duration.ofMinutes(20);
        int lessonsBeforeLongBreak = 2;
        LocalTime[] shiftsBegin = {LocalTime.of(7, 30), LocalTime.of(13, 30)};
        for (int day = 1; day <= 5; day++) {
            for (int shift = 1; shift <= 2; shift++) {
                LocalTime begin = shiftsBegin[shift - 1];
                LocalTime end = begin.plus(lessonDuration);
                for (int ordinal = 1; ordinal <= 7; ordinal++) {
                    WeeklySlot lesson = new WeeklySlot(
                        DayOfWeek.of(day), shift, ordinal, begin, end);
                    weeklySlotRepository.save(lesson);
                    if (ordinal == lessonsBeforeLongBreak) {
                        begin = end.plus(longBreakDuration);
                    }
                    else {
                        begin = end.plus(breakDuration);
                    }
                    end = begin.plus(lessonDuration);
                }
            }
        }
    }

    private void createStudents() {
        Student student = new Student(
            new User("гошко"),
            grades.get(9));
        student.getUser().getPersonalInfo()
            .setFirstName("Гошко")
            .setMiddleName("Георгиев")
            .setLastName("Гошев");
        student.getMarks().add(new Mark(student, subjects.get(0), 599));
        studentRepository.save(student);
    }
}