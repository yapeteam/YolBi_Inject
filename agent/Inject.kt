import ClientType.*
import com.sun.tools.attach.VirtualMachine.attach
import sun.jvmstat.monitor.MonitoredHost
import sun.jvmstat.monitor.MonitoredVmUtil
import sun.jvmstat.monitor.VmIdentifier
import java.io.File
import java.io.IOException


fun getPid(type: ClientType = PCL): String? {
    val classname =
        when (type) {
            Lunar -> "com.moonsworth.lunar.genesis.Genesis"
            Minecraft -> "net.minecraft.client.main.Main"
            Forge -> "net.minecraft.launchwrapper.Launch"
            PCL -> "JavaWrapper.jar"
        }
    val local = MonitoredHost.getMonitoredHost("localhost")
    local.activeVms().forEach {
        val vm = local.getMonitoredVm(VmIdentifier("//$it"))
        val processName = MonitoredVmUtil.mainClass(vm, true)
        println(processName)
        if (processName.contains(classname)) {
            return it.toString()
        }
    }
    return null
}


fun main() {
    val agentFile = File("injector/agent.dll")
    try {
        val pid = getPid()
        attach(pid).loadAgentPath(agentFile.absolutePath, "")
        println("注入成功")
    } catch (exception: Exception) {
        if (exception is IOException) {
            println("可能是版本不匹配无需在意")
            println("注入成功")
        } else {
            println("注入失败")
            throw RuntimeException(exception)
        }
    }
}
