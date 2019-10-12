function initializeCoreMod() {

	INVOKESTATIC = Java.type("org.objectweb.asm.Opcodes").INVOKESTATIC;

	return {
		"NoCubes Hooks": {
			"target": {
				"type": "CLASS",
				"name": "io.github.cadiboo.nocubes.hooks.Hooks"
			},
			"transformer": function(classNode) {
				classNode.methods.forEach(function(m) {
					for (var it = m.instructions.iterator(); it.hasNext();) {
						var i = it.next();
						if (i.getOpcode() == INVOKESTATIC && i.owner.equals("io/github/cadiboo/nocubes/client/render/RenderDispatcher"))
							i.owner = "io/github/cadiboo/nocubes/client/render/NewRenderDispatcher";
					}
				});
				return classNode;
			}
		}
	}
}
