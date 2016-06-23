import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Test

/**
 * Created by Administrator on 2016/6/23.
 */
class PackagePluginTest {
    @Test
    public void testGetTaskName() {
        Project project = ProjectBuilder.builder().build()
        project.task("hello") << {
            println 'hello swallow'
        }
        project.plugins.apply 'com.android.application'
        project.plugins.apply 'org.kunyang.tools.apk-pkg'

        project.extensions.android.buildTypes{
            alpha{

            }
            aaaa{

            }
            bbbb{

            }
            ccc{

            }
        }

        for (String name:project.tasks.getNames()){
            println 'dddddddddddddd   '+name
        }
    }
}
