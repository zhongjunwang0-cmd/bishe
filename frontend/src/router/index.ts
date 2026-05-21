import { createRouter, createWebHistory } from 'vue-router'
import { ElMessage } from 'element-plus'

const routes = [
    {
        path: '/login',
        name: 'Login',
        component: () => import('../views/Login.vue')
    },
    {
        path: '/',
        component: () => import('../layout/MainLayout.vue'),
        children: [
            {
                path: '',
                name: 'Home',
                component: () => import('../views/Home.vue')
            },
            {
                path: 'vocab',
                name: 'Vocab',
                component: () => import('../views/VocabList.vue')
            },
            {
                path: 'grammar',
                name: 'Grammar',
                component: () => import('../views/GrammarList.vue')
            },
            {
                path: 'literature',
                name: 'Literature',
                component: () => import('../views/LiteratureList.vue')
            },
            {
                path: 'oral',
                name: 'Oral',
                component: () => import('../views/OralPractice.vue')
            },
            {
                path: 'reading',
                name: 'Reading',
                component: () => import('../views/ReadingComprehension.vue')
            },
            {
                path: 'listening',
                name: 'Listening',
                component: () => import('../views/ListeningTraining.vue')
            },
            {
                path: 'cloze',
                name: 'Cloze',
                component: () => import('../views/ClozeTest.vue')
            },
            {
                path: 'record',
                name: 'Record',
                component: () => import('../views/LearningRecord.vue')
            },
            {
                path: 'discussion',
                name: 'Discussion',
                component: () => import('../views/DiscussionArea.vue')
            },
            {
                path: 'ai-tutoring',
                name: 'AiTutoring',
                component: () => import('../views/AiTutoring.vue')
            },
            {
                path: 'profile',
                name: 'Profile',
                component: () => import('../views/PersonalCenter.vue')
            },
            // ── Teacher + Admin ──
            {
                path: 'admin/tests',
                name: 'TestManagement',
                meta: { roles: ['Admin', 'Teacher'] },
                component: () => import('../views/TestManagement.vue')
            },
            // ── Admin only ──
            {
                path: 'admin',
                name: 'AdminDashboard',
                meta: { roles: ['Admin'] },
                component: () => import('../views/AdminDashboard.vue')
            },
            {
                path: 'admin/users',
                name: 'UserManagement',
                meta: { roles: ['Admin'] },
                component: () => import('../views/UserManagement.vue')
            },
            {
                path: 'admin/settings',
                name: 'SystemSettings',
                meta: { roles: ['Admin'] },
                component: () => import('../views/SystemSettings.vue')
            },
            {
                path: 'admin/vocab',
                name: 'VocabManagement',
                meta: { roles: ['Admin'] },
                component: () => import('../views/VocabList.vue')
            },
            {
                path: 'admin/grammar',
                name: 'GrammarManagement',
                meta: { roles: ['Admin'] },
                component: () => import('../views/GrammarList.vue')
            },
            {
                path: 'admin/literature',
                name: 'LiteratureManagement',
                meta: { roles: ['Admin'] },
                component: () => import('../views/LiteratureList.vue')
            },
            {
                path: 'admin/oral',
                name: 'OralManagement',
                meta: { roles: ['Admin'] },
                component: () => import('../views/OralPractice.vue')
            },
            {
                path: 'admin/discussions',
                name: 'DiscussionManagement',
                meta: { roles: ['Admin'] },
                component: () => import('../views/DiscussionArea.vue')
            },
            {
                path: 'admin/tools',
                name: 'ToolsIntegration',
                meta: { roles: ['Admin'] },
                component: () => import('../views/ToolsIntegration.vue')
            }
        ]
    }
]

const router = createRouter({
    history: createWebHistory(),
    routes
})

router.beforeEach((to, _from, next) => {
    const isLoggedIn = !!localStorage.getItem('userRole')
    if (to.path !== '/login' && !isLoggedIn) {
        next('/login')
        return
    }
    const requiredRoles = to.meta?.roles as string[] | undefined
    if (requiredRoles && requiredRoles.length > 0) {
        const userRole = localStorage.getItem('userRole') || 'User'
        if (!requiredRoles.includes(userRole)) {
            ElMessage.error('权限不足，无法访问该页面')
            next('/')
            return
        }
    }
    next()
})

export default router
