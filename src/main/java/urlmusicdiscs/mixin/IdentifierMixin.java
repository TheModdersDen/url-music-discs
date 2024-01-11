package urlmusicdiscs.mixin;

import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Identifier.class)
public class IdentifierMixin {

	/**
	 * Cancels the validatePath method.
	 * @param namespace The namespace of the identifier to validate the path of (String).
	 * @param path The path of the identifier to validate (String).
	 * @param cir The callback info returnable (String).
	 */
	@Inject(at = @At("HEAD"), method = "validatePath", cancellable = true)
	private static void validatePath(String namespace, String path, CallbackInfoReturnable<String> cir) {
		if (namespace.equals("urlmusicdiscs")) {
			cir.setReturnValue(path);
			cir.cancel();
		}
	}
}