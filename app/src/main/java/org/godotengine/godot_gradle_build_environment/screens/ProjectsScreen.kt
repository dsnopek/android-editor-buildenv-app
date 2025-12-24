package org.godotengine.godot_gradle_build_environment.screens

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.godotengine.godot_gradle_build_environment.AppPaths
import org.godotengine.godot_gradle_build_environment.CachedProject
import org.godotengine.godot_gradle_build_environment.FileUtils
import org.godotengine.godot_gradle_build_environment.ProjectInfo

@Composable
fun ProjectsScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val projects = remember { loadCachedProjects(context) }
    val sizeCache = remember { mutableStateMapOf<String, Long>() }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
    ) {
        Text(
            text = "Projects",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (projects.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = "No cached projects",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(projects) { project ->
                    ProjectItem(project, sizeCache)
                }
            }
        }
    }
}

@Composable
private fun ProjectItem(
    project: CachedProject,
    sizeCache: MutableMap<String, Long>
) {
    val cacheKey = project.cacheDirectory.absolutePath
    var sizeText by remember { mutableStateOf<String?>(null) }

    // Load size asynchronously and cache it
    LaunchedEffect(cacheKey) {
        val cachedSize = sizeCache[cacheKey]
        if (cachedSize != null) {
            sizeText = FileUtils.formatSize(cachedSize)
        } else {
            // Calculate size in background thread
            val size = withContext(Dispatchers.IO) {
                FileUtils.calculateDirectorySize(project.cacheDirectory)
            }
            sizeCache[cacheKey] = size
            sizeText = FileUtils.formatSize(size)
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = project.info.getProjectName(),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = project.info.projectPath,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )
            Text(
                text = sizeText ?: "Calculating...",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

private fun loadCachedProjects(context: Context): List<CachedProject> {
    val projectsDir = AppPaths.getProjectDir(context)
    return ProjectInfo.getAllCachedProjects(projectsDir)
}
