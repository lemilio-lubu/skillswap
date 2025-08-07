package com.emilio.jacome.skillswap.utils

import com.emilio.jacome.skillswap.model.User
import com.emilio.jacome.skillswap.model.Skill

object UserProfileCache {
    
    private var cachedUser: User? = null
    private var cachedSkills: List<Skill>? = null
    private var lastUserUpdate: Long = 0
    private var lastSkillsUpdate: Long = 0
    
    // Cache válido por 5 minutos para datos del usuario
    private val USER_CACHE_DURATION = 5 * 60 * 1000L
    // Cache de skills se invalida solo cuando se modifica
    
    fun getCachedUser(): User? {
        val currentTime = System.currentTimeMillis()
        return if (currentTime - lastUserUpdate < USER_CACHE_DURATION) {
            cachedUser
        } else {
            null
        }
    }
    
    fun setCachedUser(user: User) {
        cachedUser = user
        lastUserUpdate = System.currentTimeMillis()
    }
    
    fun getCachedSkills(): List<Skill>? {
        return cachedSkills
    }
    
    fun setCachedSkills(skills: List<Skill>) {
        cachedSkills = skills
        lastSkillsUpdate = System.currentTimeMillis()
    }
    
    fun invalidateSkillsCache() {
        cachedSkills = null
        lastSkillsUpdate = 0
    }
    
    fun invalidateUserCache() {
        cachedUser = null
        lastUserUpdate = 0
    }
    
    fun invalidateAll() {
        invalidateUserCache()
        invalidateSkillsCache()
    }
    
    fun hasValidSkillsCache(): Boolean {
        return cachedSkills != null
    }
    
    fun hasValidUserCache(): Boolean {
        val currentTime = System.currentTimeMillis()
        return cachedUser != null && currentTime - lastUserUpdate < USER_CACHE_DURATION
    }
}
