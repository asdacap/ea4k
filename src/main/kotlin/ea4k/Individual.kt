package ea4k

/**
 * F for fitness
 */
interface Individual<F> {
    // Ideally we don't want the fitness exactly in the individual
    // But.. its just a lot easier like this. Like... what about cloning the fitness?
    var fitness: F?
}