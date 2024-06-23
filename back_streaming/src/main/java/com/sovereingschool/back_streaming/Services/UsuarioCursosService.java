package com.sovereingschool.back_streaming.Services;

import org.springframework.stereotype.Service;

import com.sovereingschool.back_streaming.Interfaces.IUsuarioCursos;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class UsuarioCursosService implements IUsuarioCursos {

    /*
     * @Autowired
     * private UserRepository userRepository; // Repositorio de PostgreSQL para
     * usuarios
     * 
     * @Autowired
     * private CourseRepository courseRepository; // Repositorio de PostgreSQL para
     * cursos
     * 
     * @Autowired
     * private ClassRepository classRepository; // Repositorio de PostgreSQL para
     * clases
     * 
     * @Autowired
     * private UserCoursesRepository userCoursesRepository; // Repositorio de
     * MongoDB
     * 
     * public void syncUserCourses() {
     * List<User> users = userRepository.findAll();
     * for (User user : users) {
     * List<Course> courses = courseRepository.findByUserId(user.getId());
     * List<CourseStatus> courseStatuses = courses.stream().map(course -> {
     * List<Class> classes = classRepository.findByCourseId(course.getId());
     * List<ClassStatus> classStatuses = classes.stream().map(clazz -> {
     * ClassStatus classStatus = new ClassStatus();
     * classStatus.setClassId(clazz.getId());
     * classStatus.setCompleted(false); // Default value, modify as needed
     * classStatus.setProgress(0); // Default value, modify as needed
     * return classStatus;
     * }).collect(Collectors.toList());
     * 
     * CourseStatus courseStatus = new CourseStatus();
     * courseStatus.setCourseId(course.getId());
     * courseStatus.setClasses(classStatuses);
     * return courseStatus;
     * }).collect(Collectors.toList());
     * 
     * UserCourses userCourses = new UserCourses();
     * userCourses.setUserId(user.getId());
     * userCourses.setCourses(courseStatuses);
     * 
     * userCoursesRepository.save(userCourses);
     * }
     * }
     */
}
