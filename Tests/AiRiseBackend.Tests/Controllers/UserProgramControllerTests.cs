using System.Threading;
using System.Threading.Tasks;
using AiRise.Controllers;
using AiRise.Models.User;
using AiRise.Services;
using Microsoft.AspNetCore.Mvc;
using Moq;
using Xunit;

public class UserProgramController_Tests
{
    [Fact]
    public async Task Get_Returns_Ok_With_Doc_When_Found()
    {
        var svc = new Mock<IUserProgramService>();
        var doc = new UserProgramDoc { FirebaseUid = "u1", Program = new UserProgram { TemplateName = "T", Days = 3 } };
        svc.Setup(s => s.GetAsync("u1", It.IsAny<CancellationToken>())).ReturnsAsync(doc);

        var ctrl = new UserProgramController(svc.Object, Mock.Of<Microsoft.Extensions.Logging.ILogger<UserProgramController>>());

        var res = await ctrl.Get("u1", CancellationToken.None);

        var ok = Assert.IsType<OkObjectResult>(res.Result);
        var payload = Assert.IsType<UserProgramDoc>(ok.Value);
        Assert.Equal("u1", payload.FirebaseUid);
    }

    [Fact]
    public async Task Get_Returns_NotFound_When_Missing()
    {
        var svc = new Mock<IUserProgramService>();
        svc.Setup(s => s.GetAsync("missing", It.IsAny<CancellationToken>())).ReturnsAsync((UserProgramDoc?)null);

        var ctrl = new UserProgramController(svc.Object, Mock.Of<Microsoft.Extensions.Logging.ILogger<UserProgramController>>());

        var res = await ctrl.Get("missing", CancellationToken.None);

        var nf = Assert.IsType<NotFoundObjectResult>(res.Result);
        Assert.Contains("Program not found", nf.Value!.ToString());
    }

    [Fact]
    public async Task Update_Returns_Ok_When_Service_True()
    {
        var svc = new Mock<IUserProgramService>();
        svc.Setup(s => s.UpdateAsync("u1", It.IsAny<UserProgram>(), It.IsAny<CancellationToken>())).ReturnsAsync(true);

        var ctrl = new UserProgramController(svc.Object, Mock.Of<Microsoft.Extensions.Logging.ILogger<UserProgramController>>());

        var res = await ctrl.Update("u1", new UserProgram { TemplateName = "T" }, CancellationToken.None);

        var ok = Assert.IsType<OkObjectResult>(res);
        Assert.Contains("Program updated successfully", ok.Value!.ToString());
    }

    [Fact]
    public async Task Update_Returns_NotFound_When_Service_False()
    {
        var svc = new Mock<IUserProgramService>();
        svc.Setup(s => s.UpdateAsync("u1", It.IsAny<UserProgram>(), It.IsAny<CancellationToken>())).ReturnsAsync(false);

        var ctrl = new UserProgramController(svc.Object, Mock.Of<Microsoft.Extensions.Logging.ILogger<UserProgramController>>());

        var res = await ctrl.Update("u1", new UserProgram { TemplateName = "T" }, CancellationToken.None);

        var nf = Assert.IsType<NotFoundObjectResult>(res);
        Assert.Contains("update failed", nf.Value!.ToString());
    }
}
