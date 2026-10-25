package com.ticka.app.data

import kotlin.random.Random

/** 星星种类定义（kind 索引对应 StarVisuals 的画法） */
data class StarKindDef(
    val name: String,
    val rarity: String
)

/** 16 种星星：0-5 稀有 / 7,10,11,12,14 史诗 / 6,8,9,13,15 传说 */
val starKinds = listOf(
    StarKindDef("尘星", "稀有"),     // 0
    StarKindDef("辉星", "稀有"),     // 1
    StarKindDef("焰星", "稀有"),     // 2
    StarKindDef("双子星", "稀有"),   // 3
    StarKindDef("超新星", "稀有"),   // 4
    StarKindDef("彗星", "稀有"),     // 5
    StarKindDef("极光星", "传说"),   // 6
    StarKindDef("星环星", "史诗"),   // 7
    StarKindDef("钻石星", "传说"),   // 8
    StarKindDef("脉冲星", "传说"),   // 9
    StarKindDef("星云", "史诗"),     // 10
    StarKindDef("黑曜星", "传说"),   // 11
    StarKindDef("花冠星", "史诗"),   // 12
    StarKindDef("棱星", "传说"),     // 13
    StarKindDef("月痕星", "史诗"),   // 14
    StarKindDef("冰晶星", "传说")    // 15
)

val rareKinds = listOf(0, 1, 2, 3, 4, 5)
val epicKinds = listOf(7, 10, 11, 12, 14)
val legendKinds = listOf(6, 8, 9, 13, 15)

/** 抽星：稀有 50% / 史诗 30% / 传说 20%，同稀有度内均分 */
fun rollStarKind(): Int {
    val r = Random.nextDouble()
    val pool = when {
        r < 0.50 -> rareKinds
        r < 0.80 -> epicKinds
        else -> legendKinds
    }
    return pool[Random.nextInt(pool.size)]
}
