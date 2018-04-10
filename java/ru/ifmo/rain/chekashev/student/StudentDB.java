package ru.ifmo.rain.chekashev.student;

import info.kgeorgiy.java.advanced.student.Group;
import info.kgeorgiy.java.advanced.student.Student;
import info.kgeorgiy.java.advanced.student.StudentGroupQuery;

import java.util.*;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class StudentDB implements StudentGroupQuery {

    private static final Comparator<Student> STUDENT_COMPARATOR = Comparator.comparing(Student::getLastName)
            .thenComparing(Student::getFirstName)
            .thenComparing(Student::getId);

    private Stream<Student> streamFilter(Collection<Student> collection, Predicate<Student> filter) {
        return collection.stream().filter(filter);
    }

    private Predicate<Student> predicateSample(Function<Student, String> mapper, String sample) {
        return one -> sample.equals(mapper.apply(one));
    }

    private List<Student> sortedList(Stream<Student> students, Comparator<Student> comparator) {
        return students.sorted(comparator).collect(Collectors.toList());
    }

    private List<Student> sortedList(Collection<Student> students, Comparator<Student> comparator) {
        return sortedList(students.stream(), comparator);
    }

    private List<Student> sortedList(Stream<Student> students) {
        return sortedList(students, STUDENT_COMPARATOR);
    }

    private List<Student> sortedList(Collection<Student> students) {
        return sortedList(students.stream());
    }

    private List<String> mapToString(List<Student> list, Function<Student, String> mapper) {
        return list.stream().map(mapper).collect(Collectors.toList());
    }

    private Stream<Map.Entry<String, List<Student>>> toGroupStream(Collection<Student> students) {
        return students.stream().collect(Collectors.groupingBy(Student::getGroup)).entrySet().stream();
    }

    private <T> String safeStringInject(Optional<T> one, Function<T, String> mapper) {
        return one.map(mapper).orElse("");
    }

    private List<Group> getGroups(Collection<Student> students, Comparator<Student> cmp) {
        return toGroupStream(students)
                .peek(one -> one.getValue().sort(cmp))
                .sorted(Comparator.comparing(Map.Entry::getKey, Comparator.naturalOrder()))
                .map(one -> new Group(one.getKey(), one.getValue()))
                .collect(Collectors.toList());
    }

    private String getLargest(Collection<Student> students, Function<List<Student>, Integer> mapper) {
        return safeStringInject(toGroupStream(students).map(
                one -> Map.entry(one.getKey(), mapper.apply(one.getValue())))
                .max(Comparator.<Map.Entry<String, Integer>>comparingInt(Map.Entry::getValue)
                        .thenComparing(Map.Entry::getKey, Comparator.reverseOrder())), Map.Entry::getKey);
    }

    public String getLargestGroup(Collection<Student> students) {
        return getLargest(students, List::size);
    }

    public String getLargestGroupFirstName(Collection<Student> students) {
        return getLargest(students, one -> one.stream().map(Student::getFirstName).distinct().collect(Collectors.toList()).size());
    }

    public List<Group> getGroupsById(Collection<Student> students) {
        return getGroups(students, Comparator.naturalOrder());
    }

    public List<Group> getGroupsByName(Collection<Student> students) {
        return getGroups(students, STUDENT_COMPARATOR);
    }

    public List<String> getFirstNames(List<Student> students) {
        return mapToString(students, Student::getFirstName);
    }

    public List<String> getLastNames(List<Student> students) {
        return mapToString(students, Student::getLastName);
    }

    public List<String> getGroups(List<Student> students) {
        return mapToString(students, Student::getGroup);
    }

    public List<String> getFullNames(List<Student> students) {
        return mapToString(students, one -> one.getFirstName() + " " + one.getLastName());
    }

    public Set<String> getDistinctFirstNames(List<Student> students) {
        return students.stream()
                .map(Student::getFirstName)
                .collect(Collectors.toCollection(TreeSet::new));
    }

    public String getMinStudentFirstName(List<Student> students) {
        return safeStringInject(students.stream()
                .min(Comparator.naturalOrder()), Student::getFirstName);
    }

    public List<Student> sortStudentsById(Collection<Student> students) {
        return sortedList(students, Comparator.naturalOrder());
    }

    public List<Student> sortStudentsByName(Collection<Student> students) {
        return sortedList(students);
    }

    private List<Student> findStudentsBySample(Collection<Student> students, String sample, Function<Student, String> mapper) {
        return sortedList(streamFilter(students, predicateSample(mapper, sample)));
    }

    public List<Student> findStudentsByFirstName(Collection<Student> students, String name) {
        return findStudentsBySample(students, name, Student::getFirstName);
    }

    public List<Student> findStudentsByLastName(Collection<Student> students, String name) {
        return findStudentsBySample(students, name, Student::getLastName);
    }

    public List<Student> findStudentsByGroup(Collection<Student> students, String group) {
        return findStudentsBySample(students, group, Student::getGroup);
    }

    public Map<String, String> findStudentNamesByGroup(Collection<Student> students, String group) {
        return streamFilter(students, predicateSample(Student::getGroup, group))
                .collect(Collectors.toMap(Student::getLastName, Student::getFirstName,
                        BinaryOperator.minBy(Comparator.naturalOrder())));
    }
}
