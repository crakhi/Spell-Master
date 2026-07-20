package com.spell.master.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Grade is the parent of everything else (Level -> Word). Only one grade is
 * enabled today (grade 3) but the schema already supports grades 1-10 so the
 * same tables can be filled in for the other grades later without a migration.
 */
@Entity(tableName = "grades")
data class GradeEntity(
    @PrimaryKey val gradeId: Int,
    val displayName: String,
    val isEnabled: Boolean
)
