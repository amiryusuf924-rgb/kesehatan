package id.dietku.app.domain

data class MenuPreset(
    val key: String,
    val slot: String,
    val name: String,
    val caloriesKcal: Int,
    val proteinGrams: Double,
    val proteinFamily: String,
    val ingredients: List<String>,
)

object MenuCatalog {
    val presets = listOf(
        preset("m1-a", "Makan 1", "Nasi + tempe orek + tumis kangkung", 520, 20, "tempe", "nasi", "tempe", "kangkung"),
        preset("m1-b", "Makan 1", "Nasi + telur + tahu kukus + lalapan", 500, 25, "telur", "nasi", "telur", "tahu", "lalapan"),
        preset("m1-c", "Makan 1", "Nasi + ikan pepes/pindang + sayur", 490, 31, "ikan", "nasi", "ikan", "sayur"),
        preset("m1-d", "Makan 1", "Nasi + pecel + tempe", 560, 22, "tempe", "nasi", "sayur pecel", "kacang", "tempe"),
        preset("m1-e", "Makan 1", "Nasi + sup bening tahu/telur + tempe panggang", 510, 27, "campuran", "nasi", "tahu", "telur", "tempe", "sayur sup"),
        preset("m2-a", "Makan 2", "Ikan panggang + sayur bening + nasi kecil", 430, 30, "ikan", "ikan", "sayur bening", "nasi"),
        preset("m2-b", "Makan 2", "Tahu/tempe tumis + capcay", 400, 22, "nabati", "tahu", "tempe", "sayur capcay"),
        preset("m2-c", "Makan 2", "Telur orak-arik sayur + nasi kecil", 390, 21, "telur", "telur", "sayur", "nasi"),
        preset("m2-d", "Makan 2", "Bubur kacang hijau tanpa santan + telur rebus", 420, 19, "kacang", "kacang hijau", "telur"),
        preset("m2-e", "Makan 2", "Pepes tahu/ikan + sayur rebus", 380, 27, "ikan", "tahu", "ikan", "sayur rebus"),
        preset("campus-a", "Bekal", "Nasi merah + ayam panggang + sayur", 520, 34, "ayam", "nasi merah", "dada ayam", "sayur"),
        preset("campus-b", "Bekal", "Nasi + telur + tempe + buah", 500, 24, "telur", "nasi", "telur", "tempe", "buah"),
        preset("emergency-a", "Darurat", "Telur rebus + ubi + buah", 330, 15, "telur", "telur", "ubi", "buah"),
        preset("snack-a", "Selingan", "Pepaya + kacang hijau", 180, 8, "kacang", "pepaya", "kacang hijau"),
        preset("snack-b", "Selingan", "Pisang + telur rebus", 170, 7, "telur", "pisang", "telur"),
    )

    fun forSlot(slot: String): List<MenuPreset> = presets.filter { it.slot == slot }

    fun byKey(key: String): MenuPreset? = presets.firstOrNull { it.key == key }

    private fun preset(
        key: String,
        slot: String,
        name: String,
        calories: Int,
        protein: Int,
        family: String,
        vararg ingredients: String,
    ) = MenuPreset(key, slot, name, calories, protein.toDouble(), family, ingredients.toList())
}